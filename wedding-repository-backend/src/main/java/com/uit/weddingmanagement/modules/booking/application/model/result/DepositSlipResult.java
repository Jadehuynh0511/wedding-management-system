package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import java.math.BigDecimal;
import java.time.LocalDate;

// DTO này đại diện đúng "phiếu cọc" cần đọc/in lại: giữ thông tin nhận diện tiệc
// và lồng phiếu thu cọc bên trong, không kéo theo menu/dịch vụ như màn chi tiết đầy đủ.
public record DepositSlipResult(
    Long weddingBookingId,
    Long hallId,
    String hallName,
    Long shiftId,
    String shiftName,
    String groomName,
    String brideName,
    String coupleName,
    LocalDate bookingDate,
    LocalDate celebrationDate,
    Integer tableCount,
    Integer reservedTableCount,
    BigDecimal hallTotalAmount,
    DepositReceiptResult depositReceipt) {

  public static DepositSlipResult from(WeddingBooking weddingBooking) {
    return new DepositSlipResult(
        weddingBooking.id(),
        weddingBooking.hallId(),
        weddingBooking.hallName(),
        weddingBooking.shiftId(),
        weddingBooking.shiftName(),
        weddingBooking.groomName(),
        weddingBooking.brideName(),
        weddingBooking.groomName() + " & " + weddingBooking.brideName(),
        weddingBooking.bookingDate(),
        weddingBooking.celebrationDate(),
        weddingBooking.tableCount(),
        weddingBooking.reservedTableCount(),
        weddingBooking.calculateHallTotalAmount(),
        DepositReceiptResult.from(weddingBooking.depositReceipt()));
  }
}
