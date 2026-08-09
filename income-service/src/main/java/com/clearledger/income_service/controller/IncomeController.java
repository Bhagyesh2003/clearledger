package com.clearledger.income_service.controller;

import com.clearledger.income_service.dto.*;
import com.clearledger.income_service.service.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/income")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    // ── Streams ──────────────────────────────────────────────────────────────

    @PostMapping("/streams")
    public ResponseEntity<IncomeStreamResponse> createStream(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateStreamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(incomeService.createStream(userId, request));
    }

    @GetMapping("/streams")
    public ResponseEntity<List<IncomeStreamResponse>> getStreams(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(incomeService.getStreams(userId));
    }

    // ── Entries ──────────────────────────────────────────────────────────────

    @PostMapping("/entries")
    public ResponseEntity<IncomeEntryResponse> createEntry(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(incomeService.createEntry(userId, request));
    }

    @GetMapping("/entries")
    public ResponseEntity<List<IncomeEntryResponse>> getEntries(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String streamId) {
        return ResponseEntity.ok(incomeService.getEntries(userId, streamId));
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    @GetMapping("/summary")
    public ResponseEntity<IncomeSummaryResponse> getSummary(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {

        // Default to current month/year if not provided
        if (month == 0) month = LocalDate.now().getMonthValue();
        if (year == 0)  year  = LocalDate.now().getYear();

        return ResponseEntity.ok(incomeService.getSummary(userId, month, year));
    }
}

//@RequestHeader("X-User-Id") is the pattern every service uses.
//The gateway injects it after JWT validation.
//If someone hits this service directly without going through the gateway (no X-User-Id header),
//Spring returns 400 automatically — a second layer of protection.