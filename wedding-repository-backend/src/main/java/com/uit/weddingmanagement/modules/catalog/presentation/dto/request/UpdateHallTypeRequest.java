package com.uit.weddingmanagement.modules.catalog.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateHallTypeRequest(
    @NotBlank(message = "Hall type name is required.")
        @Size(max = 100, message = "Hall type name must not exceed 100 characters.")
        String hallTypeName,
    @DecimalMin(value = "0.01", message = "Minimum table price must be greater than 0.")
        @Digits(
            integer = 16,
            fraction = 2,
            message = "Minimum table price must have up to 16 integer digits and 2 decimal digits.")
        BigDecimal minimumTablePrice,
    @Size(max = 1000, message = "Description must not exceed 1000 characters.")
        String description) {}
