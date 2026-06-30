package com.uit.weddingmanagement.modules.booking.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateWeddingBookingMenuItemRequest(
    @NotNull(message = "Menu item id is required.")
        @Positive(message = "Menu item id must be greater than 0.")
        Long menuItemId,
    @NotNull(message = "Menu item quantity is required.")
        @Positive(message = "Menu item quantity must be greater than 0.")
        Integer quantity,
    String notes) {}
