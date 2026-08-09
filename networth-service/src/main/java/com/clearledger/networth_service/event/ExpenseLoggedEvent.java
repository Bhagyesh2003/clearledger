package com.clearledger.networth_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ExpenseLoggedEvent {
    private String expenseId;
    private String userId;
    private BigDecimal amount;
    private String category;
    private LocalDateTime timestamp;
}

//Derived from expense-service Kafka deserializes JSON into this class on the consumer side —
// it needs to exist here too.
