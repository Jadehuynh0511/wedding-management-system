package com.uit.weddingmanagement.modules.booking.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateIncidentalReceiptRequest(
    String notes,
    @NotEmpty(message = "At least one incidental service item is required.")
        List<@Valid CreateIncidentalReceiptItemRequest> items) {}
