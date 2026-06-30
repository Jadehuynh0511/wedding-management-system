package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.BookingService;
import java.math.BigDecimal;

public record WeddingBookingServiceResult(
    Long id,
    Long serviceId,
    String serviceName,
    String unitName,
    Integer quantity,
    BigDecimal priceSnapshot,
    BigDecimal lineTotal,
    String notes) {

  public static WeddingBookingServiceResult from(BookingService bookingService) {
    return new WeddingBookingServiceResult(
        bookingService.id(),
        bookingService.serviceId(),
        bookingService.serviceName(),
        bookingService.unitName(),
        bookingService.quantity(),
        bookingService.priceSnapshot(),
        bookingService.lineTotal(),
        bookingService.notes());
  }
}
