package com.uit.weddingmanagement.modules.booking.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record InvoiceSummary(
    Long id,
    Long weddingBookingId,
    Long hallId,
    String hallName,
    Long shiftId,
    String shiftName,
    String groomName,
    String brideName,
    LocalDate celebrationDate,
    Instant paidAt,
    BigDecimal finalAmount) {

  public String coupleName() {
    return groomName + " & " + brideName;
  }
}
