package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record InvoiceResponse(
    Long id,
    Long weddingBookingId,
    Long userId,
    Instant paidAt,
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
    BigDecimal finalAmount,
    String notes) {}
