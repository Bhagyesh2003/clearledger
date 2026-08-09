package com.clearledger.networth_service.controller;

import com.clearledger.networth_service.dto.*;
import com.clearledger.networth_service.entity.NetWorthSnapshot;
import com.clearledger.networth_service.service.NetWorthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NetWorthController {

    private final NetWorthService netWorthService;

    // ── Assets ────────────────────────────────────────────────────────────────

    @PostMapping("/assets")
    public ResponseEntity<AssetResponse> addAsset(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddAssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(netWorthService.addAsset(userId, request));
    }

    @GetMapping("/assets")
    public ResponseEntity<List<AssetResponse>> getAssets(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(netWorthService.getAssets(userId));
    }

    // ── Net Worth (READ SIDE — served from Redis) ─────────────────────────────

    @GetMapping("/networth/summary")
    public ResponseEntity<NetWorthSummaryResponse> getSummary(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(netWorthService.getSummary(userId));
    }

    @GetMapping("/networth/history")
    public ResponseEntity<List<NetWorthSnapshot>> getHistory(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(netWorthService.getHistory(userId));
    }

    // ── Manual recalculate (useful for testing) ───────────────────────────────

    @PostMapping("/networth/recalculate")
    public ResponseEntity<NetWorthSummaryResponse> recalculate(
            @RequestHeader("X-User-Id") String userId) {
        netWorthService.recalculate(userId);
        return ResponseEntity.ok(netWorthService.getSummary(userId));
    }
}