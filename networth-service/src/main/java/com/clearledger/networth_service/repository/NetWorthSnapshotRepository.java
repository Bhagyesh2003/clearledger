package com.clearledger.networth_service.repository;

import com.clearledger.networth_service.entity.NetWorthSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NetWorthSnapshotRepository extends JpaRepository<NetWorthSnapshot, String> {

    // Most recent snapshot — fallback when Redis cache is cold
    Optional<NetWorthSnapshot> findTopByUserIdOrderByCalculatedAtDesc(String userId);

    // All snapshots for trend chart
    List<NetWorthSnapshot> findByUserIdOrderByCalculatedAtDesc(String userId);
}