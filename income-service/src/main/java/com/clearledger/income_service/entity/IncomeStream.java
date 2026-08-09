package com.clearledger.income_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "income_streams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeStream {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // userId comes from X-User-Id header — we store it but don't join to users table
    // (users table lives in a different database — cross-service DB joins are forbidden)
    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String name;         // e.g. "Upwork Freelancing"

    private String description;   // e.g. "React/Node.js projects"

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "stream", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IncomeEntry> entries;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}