package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record InvoiceDetailResponse(
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
    List<WeddingBookingMenuItemResponse> menuItems,
    List<WeddingBookingServiceResponse> services,
    List<IncidentalReceiptResponse> incidentalReceipts) {}
