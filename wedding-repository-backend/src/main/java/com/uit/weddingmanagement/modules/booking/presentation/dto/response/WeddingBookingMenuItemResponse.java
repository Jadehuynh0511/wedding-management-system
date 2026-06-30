package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import java.math.BigDecimal;

public record WeddingBookingMenuItemResponse(
    Long id,
    Long menuItemId,
    String menuItemName,
    Integer quantity,
    BigDecimal priceSnapshot,
    BigDecimal lineTotal,
    String notes) {}
