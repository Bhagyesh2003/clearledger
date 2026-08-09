package com.clearledger.networth_service.service;

import com.clearledger.networth_service.client.DebtServiceClient;
import com.clearledger.networth_service.dto.*;
import com.clearledger.networth_service.entity.Asset;
import com.clearledger.networth_service.entity.NetWorthSnapshot;
import com.clearledger.networth_service.repository.AssetRepository;
import com.clearledger.networth_service.repository.NetWorthSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NetWorthService {

    private final AssetRepository assetRepository;
    private final NetWorthSnapshotRepository snapshotRepository;
    private final DebtServiceClient debtServiceClient;
    private final RedisTemplate<String, NetWorthSummaryResponse> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "networth:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    // ─────────────────────────────────────────────────────────────────────────
    // WRITE SIDE (Command) — triggered by Kafka event or asset change
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void recalculate(String userId) {
        log.info("Recalculating net worth for user: {}", userId);

        // 1. Sum all assets from our own DB
        BigDecimal totalAssets = assetRepository.getTotalAssetValue(userId);

        // 2. Get total liabilities from Debt Service (synchronous REST call)
        BigDecimal totalLiabilities = debtServiceClient.getTotalOutstanding(userId);

        // 3. Compute net worth
        BigDecimal netWorth = totalAssets.subtract(totalLiabilities);

        // 4. Save snapshot to DB (the historical record)
        NetWorthSnapshot snapshot = NetWorthSnapshot.builder()
                .userId(userId)
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .netWorth(netWorth)
                .calculatedAt(LocalDateTime.now())
                .build();
        snapshotRepository.save(snapshot);

        // 5. Update Redis cache — invalidate old, write fresh value
        NetWorthSummaryResponse summary = NetWorthSummaryResponse.builder()
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .netWorth(netWorth)
                .calculatedAt(snapshot.getCalculatedAt())
                .source("cache")
                .build();

        String cacheKey = CACHE_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(cacheKey, summary, CACHE_TTL);
        log.info("Net worth cached for user: {} → ₹{}", userId, netWorth);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ SIDE (Query) — always hits Redis first
    // ─────────────────────────────────────────────────────────────────────────

    public NetWorthSummaryResponse getSummary(String userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;

        // 1. Try Redis cache first
        NetWorthSummaryResponse cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("Cache hit for user: {}", userId);
            return cached;
        }

        // 2. Cache miss — fetch latest snapshot from DB
        log.info("Cache miss for user: {} — loading from DB", userId);
        return snapshotRepository.findTopByUserIdOrderByCalculatedAtDesc(userId)
                .map(s -> {
                    NetWorthSummaryResponse response = NetWorthSummaryResponse.builder()
                            .totalAssets(s.getTotalAssets())
                            .totalLiabilities(s.getTotalLiabilities())
                            .netWorth(s.getNetWorth())
                            .calculatedAt(s.getCalculatedAt())
                            .source("database")
                            .build();
                    // Re-populate cache
                    redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
                    return response;
                })
                .orElseGet(() -> {
                    // No data yet — trigger first calculation
                    recalculate(userId);
                    return redisTemplate.opsForValue().get(cacheKey);
                });
    }

    public List<NetWorthSnapshot> getHistory(String userId) {
        return snapshotRepository.findByUserIdOrderByCalculatedAtDesc(userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ASSETS — write triggers recalculation
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public AssetResponse addAsset(String userId, AddAssetRequest req) {
        Asset asset = Asset.builder()
                .userId(userId)
                .name(req.getName())
                .type(req.getType())
                .value(req.getValue())
                .build();
        asset = assetRepository.save(asset);

        // Adding an asset changes net worth — recalculate immediately
        recalculate(userId);

        return toAssetResponse(asset);
    }

    public List<AssetResponse> getAssets(String userId) {
        return assetRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toAssetResponse).toList();
    }

    private AssetResponse toAssetResponse(Asset a) {
        return AssetResponse.builder()
                .id(a.getId()).name(a.getName())
                .type(a.getType()).value(a.getValue())
                .build();
    }
}