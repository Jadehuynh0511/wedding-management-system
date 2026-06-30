package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record CancellationReceiptResponse(
    Long id,
    Long weddingBookingId,
    Long userId,
    Instant cancelledAt,
    Integer daysBeforeCelebration,
    Integer cancellationDeadlineDays,
    BigDecimal configuredDepositRefundPercentage,
    BigDecimal appliedDepositRefundPercentage,
    BigDecimal refundAmount,
    String reason) {}
