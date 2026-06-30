package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WeddingBookingDetailResponse(
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
    List<WeddingBookingMenuItemResponse> menuItems,
    List<WeddingBookingServiceResponse> services,
    DepositReceiptResponse depositReceipt) {}
