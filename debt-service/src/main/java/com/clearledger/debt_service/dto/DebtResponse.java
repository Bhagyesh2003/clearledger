package com.clearledger.debt_service.dto;

import com.clearledger.debt_service.entity.DebtType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class DebtResponse {
    private String id;
    private String name;
    private DebtType type;
    private BigDecimal totalAmount;
    private BigDecimal remainingAmount;
    private BigDecimal interestRate;
    private BigDecimal minimumPayment;
    private LocalDate dueDate;
    private boolean paid;
}