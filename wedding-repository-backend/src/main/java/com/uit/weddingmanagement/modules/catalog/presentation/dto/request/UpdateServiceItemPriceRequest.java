package com.uit.weddingmanagement.modules.catalog.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateServiceItemPriceRequest(
    @NotNull(message = "New service price is required.")
        @DecimalMin(value = "0.01", message = "New service price must be greater than 0.")
        BigDecimal newPrice) {}
