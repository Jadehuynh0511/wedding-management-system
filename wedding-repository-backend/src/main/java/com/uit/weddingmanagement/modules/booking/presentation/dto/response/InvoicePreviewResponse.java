package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record InvoicePreviewResponse(
    Long weddingBookingId,
    Instant calculatedAt,
    Instant graceDeadlineAt,
    BigDecimal hallTotalAmount,
    BigDecimal menuItemsTotalAmount,
    BigDecimal servicesTotalAmount,
    BigDecimal incidentalsTotalAmount,
    BigDecimal subtotalAmount,
    BigDecimal depositAmount,
    BigDecimal outstandingAmount,
    boolean latePaymentPenaltyEnabled,
    BigDecimal latePaymentPenaltyRate,
    long latePaymentPenaltyDays,
    BigDecimal latePaymentPenaltyAmount,
    BigDecimal finalAmount) {}
