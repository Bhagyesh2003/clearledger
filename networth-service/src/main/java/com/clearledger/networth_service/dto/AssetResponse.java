package com.clearledger.networth_service.dto;

import com.clearledger.networth_service.entity.AssetType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class AssetResponse {
    private String id;
    private String name;
    private AssetType type;
    private BigDecimal value;
}