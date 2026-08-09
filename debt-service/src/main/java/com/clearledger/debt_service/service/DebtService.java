package com.clearledger.debt_service.service;

import com.clearledger.debt_service.dto.*;
import com.clearledger.debt_service.entity.Debt;
import com.clearledger.debt_service.entity.PaymentHistory;
import com.clearledger.debt_service.repository.DebtRepository;
import com.clearledger.debt_service.repository.PaymentHistoryRepository;
import com.clearledger.debt_service.strategy.AvalancheStrategy;
import com.clearledger.debt_service.strategy.DebtPayoffStrategy;
import com.clearledger.debt_service.strategy.SnowballStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @Transactional
    public DebtResponse addDebt(String userId, AddDebtRequest req) {
        Debt debt = Debt.builder()
                .userId(userId)
                .name(req.getName())
                .type(req.getType())
                .totalAmount(req.getTotalAmount())
                .remainingAmount(req.getTotalAmount()) // starts fully owed
                .interestRate(req.getInterestRate())
                .minimumPayment(req.getMinimumPayment())
                .dueDate(req.getDueDate())
                .paid(false)
                .build();
        return toResponse(debtRepository.save(debt));
    }

    public List<DebtResponse> getDebts(String userId) {
        return debtRepository.findByUserIdAndPaidFalseOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public DebtResponse recordPayment(String userId, String debtId, RecordPaymentRequest req) {

        Debt debt = debtRepository.findByIdAndUserId(debtId, userId)
                .orElseThrow(() -> new RuntimeException("Debt not found"));

        // Subtract payment from remaining amount
        BigDecimal newRemaining = debt.getRemainingAmount().subtract(req.getAmount());

        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            debt.setRemainingAmount(BigDecimal.ZERO);
            debt.setPaid(true);        // debt fully paid off
        } else {
            debt.setRemainingAmount(newRemaining);
        }
        debtRepository.save(debt);

        // Record in payment history
        PaymentHistory payment = PaymentHistory.builder()
                .debt(debt)
                .userId(userId)
                .amount(req.getAmount())
                .paidOn(req.getPaidOn())
                .build();
        paymentHistoryRepository.save(payment);

        return toResponse(debt);
    }

    // ── Payoff Plan ───────────────────────────────────────────────────────────

    public PayoffPlanResponse getPayoffPlan(String userId, String strategyName, BigDecimal monthlyBudget) {

        List<Debt> debts = debtRepository.findByUserIdAndPaidFalseOrderByCreatedAtDesc(userId);

        if (debts.isEmpty()) {
            return PayoffPlanResponse.builder()
                    .strategy(strategyName)
                    .monthlyBudget(monthlyBudget)
                    .estimatedMonthsToFreedom(0)
                    .totalInterestPaid(BigDecimal.ZERO)
                    .steps(List.of())
                    .build();
        }

        // Pick strategy based on request param
        DebtPayoffStrategy strategy = strategyName.equalsIgnoreCase("avalanche")
                ? new AvalancheStrategy()
                : new SnowballStrategy();

        List<Debt> sorted = strategy.sort(debts);

        // Build ordered steps for the response
        List<PayoffPlanResponse.DebtPayoffStep> steps = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Debt d = sorted.get(i);
            steps.add(PayoffPlanResponse.DebtPayoffStep.builder()
                    .order(i + 1)
                    .debtId(d.getId())
                    .debtName(d.getName())
                    .remainingAmount(d.getRemainingAmount())
                    .interestRate(d.getInterestRate())
                    .reason(strategy.getReason(d))
                    .build());
        }

        // Estimate months to debt freedom
        int months = estimateMonths(sorted, monthlyBudget);

        // Estimate total interest (simplified)
        BigDecimal totalInterest = estimateTotalInterest(sorted, monthlyBudget);

        return PayoffPlanResponse.builder()
                .strategy(strategyName)
                .monthlyBudget(monthlyBudget)
                .estimatedMonthsToFreedom(months)
                .totalInterestPaid(totalInterest)
                .steps(steps)
                .build();
    }

    // ── Internal endpoint for Net Worth Service ───────────────────────────────

    public BigDecimal getTotalOutstanding(String userId) {
        return debtRepository.getTotalOutstanding(userId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private int estimateMonths(List<Debt> sortedDebts, BigDecimal monthlyBudget) {
        // Simulate paying minimum on all, extra on priority debt
        BigDecimal[] remaining = sortedDebts.stream()
                .map(Debt::getRemainingAmount)
                .toArray(BigDecimal[]::new);

        BigDecimal totalMinimums = sortedDebts.stream()
                .map(Debt::getMinimumPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal extra = monthlyBudget.subtract(totalMinimums);
        if (extra.compareTo(BigDecimal.ZERO) < 0) extra = BigDecimal.ZERO;

        int months = 0;
        while (hasRemainingDebt(remaining) && months < 600) { // cap at 50 years
            months++;
            // Pay minimums on all
            for (int i = 0; i < remaining.length; i++) {
                remaining[i] = remaining[i].subtract(sortedDebts.get(i).getMinimumPayment())
                        .max(BigDecimal.ZERO);
            }
            // Throw extra at the priority debt (first unpaid in sorted order)
            for (int i = 0; i < remaining.length; i++) {
                if (remaining[i].compareTo(BigDecimal.ZERO) > 0) {
                    remaining[i] = remaining[i].subtract(extra).max(BigDecimal.ZERO);
                    break;
                }
            }
        }
        return months;
    }

    private boolean hasRemainingDebt(BigDecimal[] remaining) {
        for (BigDecimal r : remaining)
            if (r.compareTo(BigDecimal.ZERO) > 0) return true;
        return false;
    }

    private BigDecimal estimateTotalInterest(List<Debt> debts, BigDecimal monthlyBudget) {
        // Simplified: monthly interest on remaining balance summed over estimated payoff period
        int months = estimateMonths(debts, monthlyBudget);
        return debts.stream()
                .map(d -> d.getRemainingAmount()
                        .multiply(d.getInterestRate())
                        .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(months)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private DebtResponse toResponse(Debt d) {
        return DebtResponse.builder()
                .id(d.getId()).name(d.getName()).type(d.getType())
                .totalAmount(d.getTotalAmount()).remainingAmount(d.getRemainingAmount())
                .interestRate(d.getInterestRate()).minimumPayment(d.getMinimumPayment())
                .dueDate(d.getDueDate()).paid(d.isPaid())
                .build();
    }
}