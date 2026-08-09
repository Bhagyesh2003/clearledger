package com.clearledger.debt_service.controller;

import com.clearledger.debt_service.dto.*;
import com.clearledger.debt_service.service.DebtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;

    @PostMapping
    public ResponseEntity<DebtResponse> addDebt(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddDebtRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(debtService.addDebt(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<DebtResponse>> getDebts(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(debtService.getDebts(userId));
    }

    @PostMapping("/{debtId}/payment")
    public ResponseEntity<DebtResponse> recordPayment(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String debtId,
            @Valid @RequestBody RecordPaymentRequest request) {
        return ResponseEntity.ok(debtService.recordPayment(userId, debtId, request));
    }

    @GetMapping("/payoff-plan")
    public ResponseEntity<PayoffPlanResponse> getPayoffPlan(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "snowball") String strategy,
            @RequestParam(defaultValue = "10000") BigDecimal monthlyBudget) {
        return ResponseEntity.ok(debtService.getPayoffPlan(userId, strategy, monthlyBudget));
    }

    // Internal endpoint — called by Net Worth Service via RestTemplate
    // Not exposed through gateway
    @GetMapping("/total-outstanding")
    public ResponseEntity<BigDecimal> getTotalOutstanding(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(debtService.getTotalOutstanding(userId));
    }
}

//The /total-outstanding endpoint has X-User-Id because Net Worth Service will pass it when calling internally.
// In docker-compose, Net Worth calls debt-service:8084 directly — bypassing the gateway.
// The X-User-Id header gets passed manually in the RestTemplate call.