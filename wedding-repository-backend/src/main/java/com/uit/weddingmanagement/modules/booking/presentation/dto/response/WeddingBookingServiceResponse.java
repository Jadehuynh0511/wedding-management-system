package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import java.math.BigDecimal;

public record WeddingBookingServiceResponse(
    Long id,
    Long serviceId,
    String serviceName,
    String unitName,
    Integer quantity,
    BigDecimal priceSnapshot,
    BigDecimal lineTotal,
    String notes) {}
