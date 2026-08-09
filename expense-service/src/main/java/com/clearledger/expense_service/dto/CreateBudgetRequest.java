package com.clearledger.expense_service.dto;

import com.clearledger.expense_service.entity.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateBudgetRequest {

    @NotNull
    private Category category;

    @NotNull @DecimalMin("1.00")
    private BigDecimal limitAmount;

    @NotNull
    private int month;

    @NotNull
    private int year;
}