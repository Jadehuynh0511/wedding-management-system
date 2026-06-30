package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.Invoice;
import com.uit.weddingmanagement.modules.booking.domain.model.InvoiceComputation;
import java.math.BigDecimal;
import java.time.Instant;

public record InvoiceResult(
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
    String notes) {

  public static InvoiceResult from(Invoice invoice, InvoiceComputation invoiceComputation) {
    return new InvoiceResult(
        invoice.id(),
        invoice.weddingBookingId(),
        invoice.userId(),
        invoice.paidAt(),
        invoiceComputation.graceDeadlineAt(),
        invoice.hallTotalAmount(),
        invoice.menuItemsTotalAmount(),
        invoice.servicesTotalAmount(),
        invoice.incidentalsTotalAmount(),
        invoice.calculateSubtotalAmount(),
        invoice.depositAmount(),
        invoice.calculateOutstandingAmount(),
        invoiceComputation.latePaymentPenaltyEnabled(),
        invoiceComputation.latePaymentPenaltyRate(),
        invoiceComputation.latePaymentPenaltyDays(),
        invoice.latePaymentPenaltyAmount(),
        invoice.finalAmount(),
        invoice.notes());
  }
}
