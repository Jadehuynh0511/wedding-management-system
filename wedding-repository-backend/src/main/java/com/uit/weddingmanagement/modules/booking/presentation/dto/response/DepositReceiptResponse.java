package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import com.uit.weddingmanagement.modules.booking.domain.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;

public record DepositReceiptResponse(
    Long id,
    Long weddingBookingId,
    Long userId,
    Instant receivedAt,
    BigDecimal amount,
    PaymentMethod paymentMethod,
    String notes) {}
