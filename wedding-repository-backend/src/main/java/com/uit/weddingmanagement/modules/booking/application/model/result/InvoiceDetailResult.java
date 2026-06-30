package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.Invoice;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record InvoiceDetailResult(
    Long id,
    Long weddingBookingId,
    Long userId,
    Long hallId,
    String hallName,
    Long shiftId,
    String shiftName,
    String groomName,
    String brideName,
    String coupleName,
    String groomPhoneNumber,
    String bridePhoneNumber,
    LocalDate bookingDate,
    LocalDate celebrationDate,
    Integer tableCount,
    Integer reservedTableCount,
    BigDecimal tablePrice,
    Instant paidAt,
    BigDecimal hallTotalAmount,
    BigDecimal menuItemsTotalAmount,
    BigDecimal servicesTotalAmount,
    BigDecimal incidentalsTotalAmount,
    BigDecimal subtotalAmount,
    BigDecimal depositAmount,
    BigDecimal outstandingAmount,
    BigDecimal latePaymentPenaltyAmount,
    BigDecimal finalAmount,
    String notes,
    List<WeddingBookingMenuItemResult> menuItems,
    List<WeddingBookingServiceResult> services,
    List<IncidentalReceiptResult> incidentalReceipts) {

  public static InvoiceDetailResult from(
      Invoice invoice, WeddingBooking weddingBooking, List<IncidentalReceipt> incidentalReceipts) {
    return new InvoiceDetailResult(
        invoice.id(),
        invoice.weddingBookingId(),
        invoice.userId(),
        weddingBooking.hallId(),
        weddingBooking.hallName(),
        weddingBooking.shiftId(),
        weddingBooking.shiftName(),
        weddingBooking.groomName(),
        weddingBooking.brideName(),
        weddingBooking.groomName() + " & " + weddingBooking.brideName(),
        weddingBooking.groomPhoneNumber(),
        weddingBooking.bridePhoneNumber(),
        weddingBooking.bookingDate(),
        weddingBooking.celebrationDate(),
        weddingBooking.tableCount(),
        weddingBooking.reservedTableCount(),
        weddingBooking.tablePrice(),
        invoice.paidAt(),
        invoice.hallTotalAmount(),
        invoice.menuItemsTotalAmount(),
        invoice.servicesTotalAmount(),
        invoice.incidentalsTotalAmount(),
        invoice.calculateSubtotalAmount(),
        invoice.depositAmount(),
        invoice.calculateOutstandingAmount(),
        invoice.latePaymentPenaltyAmount(),
        invoice.finalAmount(),
        invoice.notes(),
        weddingBooking.menuItems().stream().map(WeddingBookingMenuItemResult::from).toList(),
        weddingBooking.services().stream().map(WeddingBookingServiceResult::from).toList(),
        incidentalReceipts.stream().map(IncidentalReceiptResult::from).toList());
  }
}
