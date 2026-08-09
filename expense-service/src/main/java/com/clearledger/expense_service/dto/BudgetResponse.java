package com.clearledger.expense_service.dto;

import com.clearledger.expense_service.entity.Category;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class BudgetResponse {
    private String id;
    private Category category;
    private BigDecimal limitAmount;
    private BigDecimal spentSoFar;    // calculated from expenses
    private BigDecimal remaining;     // limitAmount - spentSoFar
    private double percentageUsed;
    private int month;
    private int year;
}