package com.clearledger.expense_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaProducerConfig {

    // Declares the topic — Kafka creates it automatically if it doesn't exist
    @Bean
    public NewTopic expenseLoggedTopic() {
        return TopicBuilder
                .name("expense.logged")
                .partitions(1)     // 1 partition is fine for local dev
                .replicas(1)       // 1 replica since we have 1 Kafka broker
                .build();
    }
}

//partitions(1) and replicas(1) are correct for local development with a single Kafka broker.
// In production you'd use 3+ replicas for fault tolerance. If you set replicas > 1 locally,
// Kafka will throw an error because you only have one broker.