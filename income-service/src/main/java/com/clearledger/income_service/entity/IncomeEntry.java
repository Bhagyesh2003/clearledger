package com.clearledger.income_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "income_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", nullable = false)
    private IncomeStream stream;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;      // Always use BigDecimal for money — never double

    private String description;     // e.g. "Invoice #42 - E-commerce dashboard"

    @Column(nullable = false)
    private LocalDate earnedOn;     // Date the income was actually received

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}