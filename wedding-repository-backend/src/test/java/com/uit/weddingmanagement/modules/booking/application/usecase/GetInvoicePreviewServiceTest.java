package com.uit.weddingmanagement.modules.booking.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.booking.domain.model.BookingMenuItem;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingService;
import com.uit.weddingmanagement.modules.booking.domain.model.DepositReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.PaymentMethod;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.booking.application.port.out.IncidentalReceiptQueryPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetInvoicePreviewServiceTest {

  @Mock private WeddingBookingQueryPort weddingBookingQueryPort;

  @Mock private IncidentalReceiptQueryPort incidentalReceiptQueryPort;

  @Mock private ShiftQueryPort shiftQueryPort;

  @Mock private SystemParameterQueryPort systemParameterQueryPort;

  @Test
  void shouldBuildInvoicePreviewForEligibleWeddingBooking() {
    when(weddingBookingQueryPort.findWeddingBookingById(51L))
        .thenReturn(Optional.of(createWeddingBooking(WeddingBookingStatus.DA_XAC_NHAN)));
    when(shiftQueryPort.findShiftById(2L)).thenReturn(Optional.of(createShift()));
    when(systemParameterQueryPort.getSystemParameter())
        .thenReturn(Optional.of(createSystemParameter(false, "1.50")));
    when(incidentalReceiptQueryPort.sumIncidentalReceiptTotalAmountByWeddingBookingId(51L))
        .thenReturn(new BigDecimal("1500000.00"));

    GetInvoicePreviewService getInvoicePreviewService =
        new GetInvoicePreviewService(
            weddingBookingQueryPort,
            incidentalReceiptQueryPort,
            shiftQueryPort,
            systemParameterQueryPort);

    var result = getInvoicePreviewService.getInvoicePreview(51L);

    assertThat(result.weddingBookingId()).isEqualTo(51L);
    assertThat(result.incidentalsTotalAmount()).isEqualByComparingTo("1500000.00");
    assertThat(result.outstandingAmount()).isEqualByComparingTo("59500000.00");
    assertThat(result.latePaymentPenaltyEnabled()).isFalse();
    assertThat(result.latePaymentPenaltyAmount()).isEqualByComparingTo("0.00");
    assertThat(result.finalAmount()).isEqualByComparingTo("59500000.00");
  }

  @Test
  void shouldRejectPreviewForCancelledWeddingBooking() {
    when(weddingBookingQueryPort.findWeddingBookingById(51L))
        .thenReturn(Optional.of(createWeddingBooking(WeddingBookingStatus.DA_HUY)));

    GetInvoicePreviewService getInvoicePreviewService =
        new GetInvoicePreviewService(
            weddingBookingQueryPort,
            incidentalReceiptQueryPort,
            shiftQueryPort,
            systemParameterQueryPort);

    assertThatThrownBy(() -> getInvoicePreviewService.getInvoicePreview(51L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot preview invoice for a cancelled wedding booking.");
  }

  private WeddingBooking createWeddingBooking(WeddingBookingStatus status) {
    return new WeddingBooking(
        51L,
        7L,
        "Sunrise Hall",
        2L,
        "Evening",
        "Minh",
        "Lan",
        "0909000111",
        "0909000222",
        LocalDate.of(2026, 6, 1),
        LocalDate.of(2026, 8, 15),
        20,
        2,
        new BigDecimal("5000000.00"),
        status,
        "Ban gan san khau",
        List.of(
            BookingMenuItem.create(11L, "Soup cua", 1, new BigDecimal("150000.00"), null)),
        List.of(
            BookingService.create(
                21L, "Photobooth", "goi", 2, new BigDecimal("2500000.00"), null)),
        new DepositReceipt(
            300L,
            51L,
            88L,
            Instant.parse("2026-06-01T08:00:00Z"),
            new BigDecimal("50000000.00"),
            PaymentMethod.CHUYEN_KHOAN,
            null));
  }

  private Shift createShift() {
    return new Shift(2L, "Evening", LocalTime.of(18, 0), LocalTime.of(22, 0), null);
  }

  private SystemParameter createSystemParameter(boolean penaltyEnabled, String penaltyRate) {
    return new SystemParameter(
        SystemParameter.SINGLETON_ID,
        new BigDecimal("50.00"),
        penaltyEnabled,
        new BigDecimal(penaltyRate),
        15,
        new BigDecimal("50.00"));
  }
}
