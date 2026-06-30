package com.uit.weddingmanagement.modules.booking.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCancellationReceiptRequest(
    @NotBlank(message = "Cancellation reason is required.") String reason) {}
