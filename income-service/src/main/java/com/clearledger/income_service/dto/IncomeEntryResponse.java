package com.clearledger.income_service.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class IncomeEntryResponse {
    private String id;
    private String streamId;
    private String streamName;
    private BigDecimal amount;
    private String description;
    private LocalDate earnedOn;
}