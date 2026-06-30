package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.BookingMenuItem;
import java.math.BigDecimal;

public record WeddingBookingMenuItemResult(
    Long id,
    Long menuItemId,
    String menuItemName,
    Integer quantity,
    BigDecimal priceSnapshot,
    BigDecimal lineTotal,
    String notes) {

  public static WeddingBookingMenuItemResult from(BookingMenuItem bookingMenuItem) {
    return new WeddingBookingMenuItemResult(
        bookingMenuItem.id(),
        bookingMenuItem.menuItemId(),
        bookingMenuItem.menuItemName(),
        bookingMenuItem.quantity(),
        bookingMenuItem.priceSnapshot(),
        bookingMenuItem.lineTotal(),
        bookingMenuItem.notes());
  }
}
