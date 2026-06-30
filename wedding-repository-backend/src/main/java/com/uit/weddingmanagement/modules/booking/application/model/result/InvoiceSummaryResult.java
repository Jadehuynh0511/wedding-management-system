package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.InvoiceSummary;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record InvoiceSummaryResult(
    Long id,
    Long weddingBookingId,
    Long hallId,
    String hallName,
    Long shiftId,
    String shiftName,
    String groomName,
    String brideName,
    String coupleName,
    LocalDate celebrationDate,
    Instant paidAt,
    BigDecimal finalAmount) {

  public static InvoiceSummaryResult from(InvoiceSummary invoiceSummary) {
    return new InvoiceSummaryResult(
        invoiceSummary.id(),
        invoiceSummary.weddingBookingId(),
        invoiceSummary.hallId(),
        invoiceSummary.hallName(),
        invoiceSummary.shiftId(),
        invoiceSummary.shiftName(),
        invoiceSummary.groomName(),
        invoiceSummary.brideName(),
        invoiceSummary.coupleName(),
        invoiceSummary.celebrationDate(),
        invoiceSummary.paidAt(),
        invoiceSummary.finalAmount());
  }
}
