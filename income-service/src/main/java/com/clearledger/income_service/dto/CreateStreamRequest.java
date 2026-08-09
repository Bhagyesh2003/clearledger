package com.clearledger.income_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStreamRequest {

    @NotBlank(message = "Stream name is required")
    private String name;

    private String description;
}