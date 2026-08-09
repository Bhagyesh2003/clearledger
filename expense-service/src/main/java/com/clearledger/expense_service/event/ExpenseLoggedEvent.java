package com.clearledger.expense_service.event;

import com.clearledger.expense_service.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseLoggedEvent {

    private String expenseId;
    private String userId;
    private BigDecimal amount;
    private Category category;
    private LocalDateTime timestamp;
}

//This class needs @NoArgsConstructor and @AllArgsConstructor because Kafka's JsonSerializer
// uses Jackson to convert it to JSON, and Jackson needs a no-args constructor to deserialize.
// Without it you'll get a serialization error.