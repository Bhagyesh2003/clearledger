package com.clearledger.debt_service.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class PayoffPlanResponse {

    private String strategy;               // "snowball" or "avalanche"
    private BigDecimal monthlyBudget;      // total available to pay debts each month
    private int estimatedMonthsToFreedom;  // how many months to pay everything off
    private BigDecimal totalInterestPaid;  // total interest across all debts

    private List<DebtPayoffStep> steps;   // ordered list of which debt to attack first

    @Data @Builder
    public static class DebtPayoffStep {
        private int order;                 // 1 = attack first
        private String debtId;
        private String debtName;
        private BigDecimal remainingAmount;
        private BigDecimal interestRate;
        private String reason;             // why this debt is prioritised
    }
}