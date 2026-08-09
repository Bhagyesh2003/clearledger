package com.clearledger.debt_service.dto;

import com.clearledger.debt_service.entity.DebtType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AddDebtRequest {

    @NotBlank
    private String name;

    @NotNull
    private DebtType type;

    @NotNull @DecimalMin("1.00")
    private BigDecimal totalAmount;

    @NotNull @DecimalMin("0.01")
    private BigDecimal interestRate;

    @NotNull @DecimalMin("1.00")
    private BigDecimal minimumPayment;

    private LocalDate dueDate;
}