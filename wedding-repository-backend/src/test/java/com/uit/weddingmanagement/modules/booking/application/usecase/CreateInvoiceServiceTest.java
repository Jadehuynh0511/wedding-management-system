package com.uit.weddingmanagement.modules.booking.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateInvoiceCommand;
import com.uit.weddingmanagement.modules.booking.application.port.out.IncidentalReceiptQueryPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.InvoiceCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingMenuItem;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingService;
import com.uit.weddingmanagement.modules.booking.domain.model.DepositReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.Invoice;
import com.uit.weddingmanagement.modules.booking.domain.model.PaymentMethod;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
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
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateInvoiceServiceTest {

  @Mock private WeddingBookingQueryPort weddingBookingQueryPort;

  @Mock private WeddingBookingCommandPort weddingBookingCommandPort;

  @Mock private InvoiceCommandPort invoiceCommandPort;

  @Mock private IncidentalReceiptQueryPort incidentalReceiptQueryPort;

  @Mock private ShiftQueryPort shiftQueryPort;

  @Mock private SystemParameterQueryPort systemParameterQueryPort;

  @Mock private CurrentUserPort currentUserPort;

  @Captor private ArgumentCaptor<Invoice> invoiceCaptor;

  @Test
  void shouldCreateInvoiceAndMarkWeddingBookingAsPaid() {
    WeddingBooking weddingBooking = createWeddingBooking(WeddingBookingStatus.DA_XAC_NHAN);
    AuthenticatedUser currentUser =
        new AuthenticatedUser(
            99L,
            "staff.billing",
            "Billing Staff",
            3L,
            "STAFF",
            Set.of("INVOICE_CREATE"));

    when(weddingBookingQueryPort.findWeddingBookingByIdForUpdate(51L))
        .thenReturn(Optional.of(weddingBooking));
    when(shiftQueryPort.findShiftById(2L)).thenReturn(Optional.of(createShift()));
    when(systemParameterQueryPort.getSystemParameter())
        .thenReturn(Optional.of(createSystemParameter(true, "1.50")));
    when(incidentalReceiptQueryPort.sumIncidentalReceiptTotalAmountByWeddingBookingId(51L))
        .thenReturn(new BigDecimal("1500000.00"));
    when(currentUserPort.getCurrentUser()).thenReturn(currentUser);
    when(invoiceCommandPort.saveInvoice(any(Invoice.class)))
        .thenAnswer(
            invocation -> {
              Invoice input = invocation.getArgument(0, Invoice.class);
              return new Invoice(
                  901L,
                  input.weddingBookingId(),
                  input.userId(),
                  input.paidAt(),
                  input.hallTotalAmount(),
                  input.menuItemsTotalAmount(),
                  input.servicesTotalAmount(),
                  input.incidentalsTotalAmount(),
                  input.depositAmount(),
                  input.latePaymentPenaltyAmount(),
                  input.finalAmount(),
                  input.notes());
            });
    when(weddingBookingCommandPort.updateWeddingBookingStatus(51L, WeddingBookingStatus.DA_THANH_TOAN))
        .thenReturn(createWeddingBooking(WeddingBookingStatus.DA_THANH_TOAN));

    CreateInvoiceService createInvoiceService =
        new CreateInvoiceService(
            weddingBookingQueryPort,
            weddingBookingCommandPort,
            invoiceCommandPort,
            incidentalReceiptQueryPort,
            shiftQueryPort,
            systemParameterQueryPort,
            currentUserPort);

    var result = createInvoiceService.createInvoice(51L, new CreateInvoiceCommand("Thanh toan lan cuoi"));

    assertThat(result.id()).isEqualTo(901L);
    assertThat(result.weddingBookingId()).isEqualTo(51L);
    assertThat(result.userId()).isEqualTo(99L);
    assertThat(result.subtotalAmount()).isEqualByComparingTo("109500000.00");
    assertThat(result.outstandingAmount()).isEqualByComparingTo("59500000.00");
    assertThat(result.latePaymentPenaltyDays()).isGreaterThanOrEqualTo(0);
    assertThat(result.finalAmount()).isGreaterThanOrEqualTo(result.outstandingAmount());
    assertThat(result.notes()).isEqualTo("Thanh toan lan cuoi");

    verify(invoiceCommandPort).saveInvoice(invoiceCaptor.capture());
    Invoice savedInvoice = invoiceCaptor.getValue();
    assertThat(savedInvoice.id()).isNull();
    assertThat(savedInvoice.weddingBookingId()).isEqualTo(51L);
    assertThat(savedInvoice.userId()).isEqualTo(99L);
    assertThat(savedInvoice.calculateSubtotalAmount()).isEqualByComparingTo("109500000.00");
    assertThat(savedInvoice.depositAmount()).isEqualByComparingTo("50000000.00");
    assertThat(savedInvoice.finalAmount()).isGreaterThanOrEqualTo(new BigDecimal("59500000.00"));

    verify(weddingBookingCommandPort)
        .updateWeddingBookingStatus(51L, WeddingBookingStatus.DA_THANH_TOAN);
  }

  @Test
  void shouldRejectWhenWeddingBookingIsAlreadyPaid() {
    when(weddingBookingQueryPort.findWeddingBookingByIdForUpdate(51L))
        .thenReturn(Optional.of(createWeddingBooking(WeddingBookingStatus.DA_THANH_TOAN)));

    CreateInvoiceService createInvoiceService =
        new CreateInvoiceService(
            weddingBookingQueryPort,
            weddingBookingCommandPort,
            invoiceCommandPort,
            incidentalReceiptQueryPort,
            shiftQueryPort,
            systemParameterQueryPort,
            currentUserPort);

    assertThatThrownBy(() -> createInvoiceService.createInvoice(51L, new CreateInvoiceCommand(null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot create invoice for a fully paid wedding booking.");

    verify(invoiceCommandPort, never()).saveInvoice(any(Invoice.class));
    verify(weddingBookingCommandPort, never())
        .updateWeddingBookingStatus(any(Long.class), any(WeddingBookingStatus.class));
  }

  @Test
  void shouldRejectWhenWeddingBookingIsCancelled() {
    when(weddingBookingQueryPort.findWeddingBookingByIdForUpdate(51L))
        .thenReturn(Optional.of(createWeddingBooking(WeddingBookingStatus.DA_HUY)));

    CreateInvoiceService createInvoiceService =
        new CreateInvoiceService(
            weddingBookingQueryPort,
            weddingBookingCommandPort,
            invoiceCommandPort,
            incidentalReceiptQueryPort,
            shiftQueryPort,
            systemParameterQueryPort,
            currentUserPort);

    assertThatThrownBy(() -> createInvoiceService.createInvoice(51L, new CreateInvoiceCommand(null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot create invoice for a cancelled wedding booking.");

    verify(invoiceCommandPort, never()).saveInvoice(any(Invoice.class));
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
