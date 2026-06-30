package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceipt;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record IncidentalReceiptResult(
    Long id,
    Long weddingBookingId,
    Long userId,
    Instant recordedAt,
    BigDecimal totalAmount,
    String notes,
    List<IncidentalReceiptItemResult> items) {

  public static IncidentalReceiptResult from(IncidentalReceipt incidentalReceipt) {
    return new IncidentalReceiptResult(
        incidentalReceipt.id(),
        incidentalReceipt.weddingBookingId(),
        incidentalReceipt.userId(),
        incidentalReceipt.recordedAt(),
        incidentalReceipt.totalAmount(),
        incidentalReceipt.notes(),
        incidentalReceipt.items().stream().map(IncidentalReceiptItemResult::from).toList());
  }
}
