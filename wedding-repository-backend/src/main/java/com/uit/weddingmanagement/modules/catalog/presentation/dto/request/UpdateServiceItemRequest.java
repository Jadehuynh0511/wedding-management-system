package com.uit.weddingmanagement.modules.catalog.presentation.dto.request;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateServiceItemRequest(
    @NotBlank(message = "Service item name is required.") String serviceName,
    @NotBlank(message = "Service item category is required.") String serviceCategory,
    @NotBlank(message = "Service item unit name is required.") String unitName,
    @NotNull(message = "Service item status is required.") ServiceItemStatus status,
    String description) {}
