package com.uit.weddingmanagement.modules.booking.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateWeddingBookingServiceRequest(
    @NotNull(message = "Service id is required.")
        @Positive(message = "Service id must be greater than 0.")
        Long serviceId,
    @NotNull(message = "Service quantity is required.")
        @Positive(message = "Service quantity must be greater than 0.")
        Integer quantity,
    String notes) {}
