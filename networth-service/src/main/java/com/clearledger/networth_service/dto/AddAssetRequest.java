package com.clearledger.networth_service.dto;

import com.clearledger.networth_service.entity.AssetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddAssetRequest {

    @NotBlank
    private String name;

    @NotNull
    private AssetType type;

    @NotNull @DecimalMin("0.00")
    private BigDecimal value;
}