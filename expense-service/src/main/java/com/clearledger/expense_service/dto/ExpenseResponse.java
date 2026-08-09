package com.clearledger.expense_service.dto;

import com.clearledger.expense_service.entity.Category;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class ExpenseResponse {
    private String id;
    private BigDecimal amount;
    private Category category;
    private String description;
    private LocalDate spentOn;
}