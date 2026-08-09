package com.clearledger.networth_service.consumer;

import com.clearledger.networth_service.event.ExpenseLoggedEvent;
import com.clearledger.networth_service.service.NetWorthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseEventConsumer {

    private final NetWorthService netWorthService;

    @KafkaListener(
            topics = "expense.logged",
            groupId = "networth-consumer-group"
    )
    public void onExpenseLogged(ExpenseLoggedEvent event) {
        log.info("Received expense.logged event for user: {} amount: {}",
                event.getUserId(), event.getAmount());

        // Trigger net worth recalculation for this user
        // This calls Debt Service + sums assets + saves snapshot + updates Redis
        netWorthService.recalculate(event.getUserId());
    }
}

//This class has one job: listen to the expense.logged topic. Whenever Expense Service publishes an event,
// this method wakes up automatically, extracts the userId, and triggers a net worth recalculation.
// The user who logged the expense sees their 201 response immediately — this happens silently in the background.

//@KafkaListener is all Spring needs to know this method is a consumer.
// The groupId ensures that if you run multiple instances of networth-service,
// only one instance processes each event — Kafka handles the load balancing automatically within a consumer group.