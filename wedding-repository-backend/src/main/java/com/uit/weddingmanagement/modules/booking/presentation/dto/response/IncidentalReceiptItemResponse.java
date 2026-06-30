package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import java.math.BigDecimal;

public record IncidentalReceiptItemResponse(
    Long id,
    Long serviceId,
    String serviceName,
    String unitName,
    Integer quantity,
    BigDecimal appliedUnitPrice,
    BigDecimal lineTotal,
    String notes) {}
