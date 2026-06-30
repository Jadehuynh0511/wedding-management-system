package com.uit.weddingmanagement.modules.booking.application.usecase;

import com.uit.weddingmanagement.modules.booking.application.model.result.InvoicePreviewResult;
import com.uit.weddingmanagement.modules.booking.application.port.in.GetInvoicePreviewUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.IncidentalReceiptQueryPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.InvoiceCalculator;
import com.uit.weddingmanagement.modules.booking.domain.model.InvoiceComputation;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetInvoicePreviewService implements GetInvoicePreviewUseCase {

  private final WeddingBookingQueryPort weddingBookingQueryPort;
  private final IncidentalReceiptQueryPort incidentalReceiptQueryPort;
  private final ShiftQueryPort shiftQueryPort;
  private final SystemParameterQueryPort systemParameterQueryPort;

  public GetInvoicePreviewService(
      WeddingBookingQueryPort weddingBookingQueryPort,
      IncidentalReceiptQueryPort incidentalReceiptQueryPort,
      ShiftQueryPort shiftQueryPort,
      SystemParameterQueryPort systemParameterQueryPort) {
    this.weddingBookingQueryPort = weddingBookingQueryPort;
    this.incidentalReceiptQueryPort = incidentalReceiptQueryPort;
    this.shiftQueryPort = shiftQueryPort;
    this.systemParameterQueryPort = systemParameterQueryPort;
  }

  // Lấy thông tin tạm tính hóa đơn cho booking, kh tạo bản ghi hóa đơn nào trong DB cả
  // Sử dụng để hiển thị preview hóa đơn trước khi tạo chính thức
  @Override
  public InvoicePreviewResult getInvoicePreview(Long bookingId) {
    requirePositiveBookingId(bookingId);

    WeddingBooking weddingBooking = loadWeddingBooking(bookingId);
    ensureBookingEligibleForPayment(weddingBooking);

    Shift shift = loadShift(weddingBooking.shiftId());
    SystemParameter systemParameter = loadSystemParameter();
    BigDecimal incidentalsTotalAmount =
        incidentalReceiptQueryPort.sumIncidentalReceiptTotalAmountByWeddingBookingId(bookingId);
    InvoiceComputation invoiceComputation =
        InvoiceCalculator.calculate(
            weddingBooking, shift, incidentalsTotalAmount, systemParameter, Instant.now());

    return InvoicePreviewResult.from(bookingId, invoiceComputation);
  }

  private WeddingBooking loadWeddingBooking(Long bookingId) {
    return weddingBookingQueryPort
        .findWeddingBookingById(bookingId)
        .orElseThrow(
            () -> new EntityNotFoundException("Wedding booking not found with id: " + bookingId));
  }

  private Shift loadShift(Long shiftId) {
    return shiftQueryPort
        .findShiftById(shiftId)
        .orElseThrow(() -> new EntityNotFoundException("Shift not found with id: " + shiftId));
  }

  private SystemParameter loadSystemParameter() {
    return systemParameterQueryPort
        .getSystemParameter()
        .orElseThrow(() -> new EntityNotFoundException("System parameter row was not found."));
  }

  private void ensureBookingEligibleForPayment(WeddingBooking weddingBooking) {
    if (weddingBooking.status() == WeddingBookingStatus.DA_THANH_TOAN) {
      throw new IllegalArgumentException(
          "Cannot preview invoice for a fully paid wedding booking.");
    }

    if (weddingBooking.status() == WeddingBookingStatus.DA_HUY) {
      throw new IllegalArgumentException(
          "Cannot preview invoice for a cancelled wedding booking.");
    }
  }

  private void requirePositiveBookingId(Long bookingId) {
    if (bookingId == null) {
      throw new IllegalArgumentException("Wedding booking id is required.");
    }

    if (bookingId <= 0) {
      throw new IllegalArgumentException("Wedding booking id must be greater than 0.");
    }
  }
}
