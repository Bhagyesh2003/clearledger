package com.clearledger.expense_service.service;

import com.clearledger.expense_service.event.ExpenseLoggedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

//This is the class that actually sends events. It wraps KafkaTemplate — think of KafkaTemplate
// the same way you'd think of JdbcTemplate. It's the low-level tool, and we wrap it in a service with a clean method.

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, ExpenseLoggedEvent> kafkaTemplate;

    public void publishExpenseLogged(ExpenseLoggedEvent event) {
        kafkaTemplate.send("expense.logged", event.getUserId(), event);
        log.info("Published expense.logged event for user: {}", event.getUserId());
    }
}

//kafkaTemplate.send(topic, key, value). The key is the userId — Kafka uses this to ensure all events
// for the same user go to the same partition, preserving order. The value is our event object which
// gets serialized to JSON automatically.