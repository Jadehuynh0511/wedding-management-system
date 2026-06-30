package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.InvoiceComputation;
import java.math.BigDecimal;
import java.time.Instant;

public record InvoicePreviewResult(
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
    BigDecimal finalAmount) {

  public static InvoicePreviewResult from(Long weddingBookingId, InvoiceComputation invoiceComputation) {
    return new InvoicePreviewResult(
        weddingBookingId,
        invoiceComputation.calculatedAt(),
        invoiceComputation.graceDeadlineAt(),
        invoiceComputation.hallTotalAmount(),
        invoiceComputation.menuItemsTotalAmount(),
        invoiceComputation.servicesTotalAmount(),
        invoiceComputation.incidentalsTotalAmount(),
        invoiceComputation.subtotalAmount(),
        invoiceComputation.depositAmount(),
        invoiceComputation.outstandingAmount(),
        invoiceComputation.latePaymentPenaltyEnabled(),
        invoiceComputation.latePaymentPenaltyRate(),
        invoiceComputation.latePaymentPenaltyDays(),
        invoiceComputation.latePaymentPenaltyAmount(),
        invoiceComputation.finalAmount());
  }
}
