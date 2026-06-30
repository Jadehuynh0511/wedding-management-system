package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record IncidentalReceiptResponse(
    Long id,
    Long weddingBookingId,
    Long userId,
    Instant recordedAt,
    BigDecimal totalAmount,
    String notes,
    List<IncidentalReceiptItemResponse> items) {}
