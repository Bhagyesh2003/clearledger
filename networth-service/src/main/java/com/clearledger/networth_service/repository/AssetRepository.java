package com.clearledger.networth_service.repository;

import com.clearledger.networth_service.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, String> {

    List<Asset> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT COALESCE(SUM(a.value), 0) FROM Asset a WHERE a.userId = :userId")
    BigDecimal getTotalAssetValue(@Param("userId") String userId);
}