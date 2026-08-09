package com.clearledger.networth_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "networth_snapshots")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NetWorthSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAssets;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalLiabilities;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netWorth;        // totalAssets - totalLiabilities

    @Column(nullable = false)
    private LocalDateTime calculatedAt; // when this snapshot was taken
}

//Every time net worth is recalculated (triggered by Kafka event or asset update),
// a new snapshot row is saved. This gives you the history for the trend chart —
// GET /networth/history returns all snapshots ordered by time.