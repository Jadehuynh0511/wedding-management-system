package com.uit.weddingmanagement.modules.catalog.presentation.dto.request;

import com.uit.weddingmanagement.modules.catalog.domain.model.HallStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record UpdateHallRequest(
    @NotNull(message = "Hall type id is required.")
        @Positive(message = "Hall type id must be greater than 0.")
        Long hallTypeId,
    @NotBlank(message = "Hall name is required.") String hallName,
    @NotNull(message = "Max capacity is required.")
        @Positive(message = "Max capacity must be greater than 0.")
        Integer maxCapacity,
    @NotNull(message = "Table price is required.")
        @DecimalMin(value = "0.01", message = "Table price must be greater than 0.")
        BigDecimal tablePrice,
    @NotNull(message = "Hall status is required.") HallStatus status,
    String description) {}
