package com.clearledger.income_service.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class IncomeSummaryResponse {
    private int month;
    private int year;
    private BigDecimal totalIncome;
    private List<StreamSummary> byStream;

    @Data
    @Builder
    public static class StreamSummary {
        private String streamId;
        private String streamName;
        private BigDecimal total;
        private long entryCount;
    }
}