package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WeddingBookingDetailResult(
    Long id,
    Long hallId,
    String hallName,
    Long shiftId,
    String shiftName,
    String groomName,
    String brideName,
    String groomPhoneNumber,
    String bridePhoneNumber,
    LocalDate bookingDate,
    LocalDate celebrationDate,
    Integer tableCount,
    Integer reservedTableCount,
    BigDecimal tablePrice,
    BigDecimal hallTotalAmount,
    WeddingBookingStatus status,
    String notes,
    List<WeddingBookingMenuItemResult> menuItems,
    List<WeddingBookingServiceResult> services,
    DepositReceiptResult depositReceipt) {

  public static WeddingBookingDetailResult from(WeddingBooking weddingBooking) {
    return new WeddingBookingDetailResult(
        weddingBooking.id(),
        weddingBooking.hallId(),
        weddingBooking.hallName(),
        weddingBooking.shiftId(),
        weddingBooking.shiftName(),
        weddingBooking.groomName(),
        weddingBooking.brideName(),
        weddingBooking.groomPhoneNumber(),
        weddingBooking.bridePhoneNumber(),
        weddingBooking.bookingDate(),
        weddingBooking.celebrationDate(),
        weddingBooking.tableCount(),
        weddingBooking.reservedTableCount(),
        weddingBooking.tablePrice(),
        weddingBooking.calculateHallTotalAmount(),
        weddingBooking.status(),
        weddingBooking.notes(),
        weddingBooking.menuItems().stream().map(WeddingBookingMenuItemResult::from).toList(),
        weddingBooking.services().stream().map(WeddingBookingServiceResult::from).toList(),
        DepositReceiptResult.from(weddingBooking.depositReceipt()));
  }
}
