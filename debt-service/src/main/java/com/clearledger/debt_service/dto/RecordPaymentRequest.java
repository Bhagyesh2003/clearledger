package com.clearledger.debt_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecordPaymentRequest {

    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private LocalDate paidOn;
}