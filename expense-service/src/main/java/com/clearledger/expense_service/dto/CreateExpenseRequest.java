package com.clearledger.expense_service.dto;

import com.clearledger.expense_service.entity.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateExpenseRequest {

    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private Category category;

    private String description;

    @NotNull
    private LocalDate spentOn;
}