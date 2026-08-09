package com.clearledger.networth_service.config;

import com.clearledger.networth_service.dto.NetWorthSummaryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, NetWorthSummaryResponse> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, NetWorthSummaryResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Keys stored as plain strings: "networth:{userId}"
        template.setKeySerializer(new StringRedisSerializer());

        // Values stored as JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<NetWorthSummaryResponse> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, NetWorthSummaryResponse.class);

        template.setValueSerializer(serializer);
        return template;
    }
}

//Redis needs to know how to serialize our Java objects to JSON when storing them.
// By default it uses Java serialization (binary) which is unreadable.
// We configure it to use Jackson JSON instead.