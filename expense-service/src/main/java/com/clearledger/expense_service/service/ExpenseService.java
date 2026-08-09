package com.clearledger.expense_service.service;

import com.clearledger.expense_service.dto.*;
import com.clearledger.expense_service.entity.Budget;
import com.clearledger.expense_service.entity.Category;
import com.clearledger.expense_service.entity.Expense;
import com.clearledger.expense_service.event.ExpenseLoggedEvent;
import com.clearledger.expense_service.repository.BudgetRepository;
import com.clearledger.expense_service.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final KafkaProducerService kafkaProducerService;

    // ── Expenses ─────────────────────────────────────────────────────────────

    @Transactional
    public ExpenseResponse createExpense(String userId, CreateExpenseRequest req) {

        // 1. Save expense to DB
        Expense expense = Expense.builder()
                .userId(userId)
                .amount(req.getAmount())
                .category(req.getCategory())
                .description(req.getDescription())
                .spentOn(req.getSpentOn())
                .build();
        expense = expenseRepository.save(expense);

        // 2. Publish Kafka event — fire and forget
        // Net Worth Service will pick this up and recalculate
        ExpenseLoggedEvent event = ExpenseLoggedEvent.builder()
                .expenseId(expense.getId())
                .userId(userId)
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .timestamp(LocalDateTime.now())
                .build();
        kafkaProducerService.publishExpenseLogged(event);

        // 3. Return response immediately — don't wait for Net Worth
        return toResponse(expense);
    }

    public List<ExpenseResponse> getExpenses(String userId, Category category) {
        List<Expense> expenses = (category != null)
                ? expenseRepository.findByUserIdAndCategoryOrderBySpentOnDesc(userId, category)
                : expenseRepository.findByUserIdOrderBySpentOnDesc(userId);
        return expenses.stream().map(this::toResponse).toList();
    }

    // ── Budgets ───────────────────────────────────────────────────────────────

    @Transactional
    public BudgetResponse createBudget(String userId, CreateBudgetRequest req) {
        Budget budget = Budget.builder()
                .userId(userId)
                .category(req.getCategory())
                .limitAmount(req.getLimitAmount())
                .month(req.getMonth())
                .year(req.getYear())
                .build();
        budget = budgetRepository.save(budget);
        return toBudgetResponse(budget, BigDecimal.ZERO);
    }

    public List<BudgetResponse> getBudgets(String userId, int month, int year) {

        // Get all budgets for this user/month/year
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);

        // Get actual spending per category from DB
        Map<Category, BigDecimal> spendingMap = getSpendingMap(userId, month, year);

        return budgets.stream()
                .map(b -> toBudgetResponse(b, spendingMap.getOrDefault(b.getCategory(), BigDecimal.ZERO)))
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<Category, BigDecimal> getSpendingMap(String userId, int month, int year) {
        return expenseRepository.findMonthlySpendingByCategory(userId, month, year)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Category) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    private ExpenseResponse toResponse(Expense e) {
        return ExpenseResponse.builder()
                .id(e.getId())
                .amount(e.getAmount())
                .category(e.getCategory())
                .description(e.getDescription())
                .spentOn(e.getSpentOn())
                .build();
    }

    private BudgetResponse toBudgetResponse(Budget b, BigDecimal spent) {
        BigDecimal remaining = b.getLimitAmount().subtract(spent);
        double pct = spent.divide(b.getLimitAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        return BudgetResponse.builder()
                .id(b.getId())
                .category(b.getCategory())
                .limitAmount(b.getLimitAmount())
                .spentSoFar(spent)
                .remaining(remaining)
                .percentageUsed(pct)
                .month(b.getMonth())
                .year(b.getYear())
                .build();
    }
}