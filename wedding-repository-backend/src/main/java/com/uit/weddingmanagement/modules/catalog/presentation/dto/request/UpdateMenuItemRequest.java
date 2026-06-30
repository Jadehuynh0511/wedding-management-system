package com.uit.weddingmanagement.modules.catalog.presentation.dto.request;

import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateMenuItemRequest(
    @NotBlank(message = "Menu item name is required.") String itemName,
    @NotBlank(message = "Menu item category is required.") String itemCategory,
    @NotNull(message = "Menu item current price is required.")
        @DecimalMin(value = "0.01", message = "Menu item current price must be greater than 0.")
        BigDecimal currentPrice,
    @NotNull(message = "Menu item status is required.") MenuItemStatus status,
    String description) {}
