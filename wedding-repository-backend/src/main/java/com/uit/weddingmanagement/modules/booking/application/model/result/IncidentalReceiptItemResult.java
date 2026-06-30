package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceiptItem;
import java.math.BigDecimal;

public record IncidentalReceiptItemResult(
    Long id,
    Long serviceId,
    String serviceName,
    String unitName,
    Integer quantity,
    BigDecimal appliedUnitPrice,
    BigDecimal lineTotal,
    String notes) {

  public static IncidentalReceiptItemResult from(IncidentalReceiptItem incidentalReceiptItem) {
    return new IncidentalReceiptItemResult(
        incidentalReceiptItem.id(),
        incidentalReceiptItem.serviceId(),
        incidentalReceiptItem.serviceName(),
        incidentalReceiptItem.unitName(),
        incidentalReceiptItem.quantity(),
        incidentalReceiptItem.appliedUnitPrice(),
        incidentalReceiptItem.lineTotal(),
        incidentalReceiptItem.notes());
  }
}
