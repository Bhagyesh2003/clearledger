package com.clearledger.income_service.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class IncomeStreamResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal totalEarned;   // aggregated from entries
    private long entryCount;
    private LocalDateTime createdAt;
}