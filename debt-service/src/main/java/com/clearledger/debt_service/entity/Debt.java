package com.clearledger.debt_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "debts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String name;               // e.g. "HDFC Credit Card"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;    // original principal

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingAmount; // what's still owed — updated on payment

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;   // annual % e.g. 18.00

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal minimumPayment; // minimum monthly payment

    private LocalDate dueDate;         // next payment due date

    private boolean paid = false;      // true when remainingAmount reaches 0

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}