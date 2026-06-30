package com.uit.weddingmanagement.modules.catalog.presentation.dto.request;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateServiceItemRequest(
    @NotBlank(message = "Service item name is required.") String serviceName,
    @NotBlank(message = "Service item category is required.") String serviceCategory,
    @NotBlank(message = "Service item unit name is required.") String unitName,
    @NotNull(message = "Service item current price is required.")
        @DecimalMin(value = "0.01", message = "Service item current price must be greater than 0.")
        BigDecimal currentPrice,
    ServiceItemStatus status,
    String description) {}
