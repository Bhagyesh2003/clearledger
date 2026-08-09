package com.clearledger.networth_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DebtServiceClient {

    private final RestTemplate restTemplate;

    @Value("${debt-service.url}")
    private String debtServiceUrl;

    public BigDecimal getTotalOutstanding(String userId) {
        try {
            // Pass X-User-Id header so Debt Service knows whose debts to sum
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = debtServiceUrl + "/api/debts/total-outstanding";
            ResponseEntity<BigDecimal> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, BigDecimal.class);

            return response.getBody() != null ? response.getBody() : BigDecimal.ZERO;

        } catch (Exception e) {
            // If Debt Service is down, return 0 rather than crashing
            // This is a simplified circuit breaker fallback
            log.warn("Could not reach debt-service, defaulting liabilities to 0: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}

//Why a dedicated client class?
//We could call RestTemplate directly inside the service, but wrapping it in a DebtServiceClient keeps the service clean.
//If we later swap RestTemplate for WebClient or Feign, we only change this one class.
//The service doesn't care how the HTTP call is made.

//The try-catch fallback to BigDecimal.ZERO is our simplified circuit breaker.
//If Debt Service is down, Net Worth still calculates using 0 liabilities — imperfect but non-crashing.
//In production you'd use Resilience4j @CircuitBreaker here with a cached last-known value as the fallback.