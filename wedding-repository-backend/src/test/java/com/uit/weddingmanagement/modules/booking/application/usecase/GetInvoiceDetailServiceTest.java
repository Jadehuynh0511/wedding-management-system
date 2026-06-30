package com.uit.weddingmanagement.modules.booking.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.booking.application.port.out.IncidentalReceiptQueryPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.InvoiceQueryPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingMenuItem;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingService;
import com.uit.weddingmanagement.modules.booking.domain.model.DepositReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceiptItem;
import com.uit.weddingmanagement.modules.booking.domain.model.Invoice;
import com.uit.weddingmanagement.modules.booking.domain.model.PaymentMethod;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetInvoiceDetailServiceTest {

  @Mock private InvoiceQueryPort invoiceQueryPort;

  @Mock private WeddingBookingQueryPort weddingBookingQueryPort;

  @Mock private IncidentalReceiptQueryPort incidentalReceiptQueryPort;

  @Test
  void shouldAssembleInvoiceDetailFromInvoiceAndSnapshots() {
    when(invoiceQueryPort.findInvoiceById(901L)).thenReturn(Optional.of(createInvoice()));
    when(weddingBookingQueryPort.findWeddingBookingById(51L))
        .thenReturn(Optional.of(createWeddingBooking()));
    when(incidentalReceiptQueryPort.findIncidentalReceiptsByWeddingBookingId(51L))
        .thenReturn(List.of(createIncidentalReceipt()));

    GetInvoiceDetailService service =
        new GetInvoiceDetailService(
            invoiceQueryPort, weddingBookingQueryPort, incidentalReceiptQueryPort);

    var result = service.getInvoiceDetail(901L);

    assertThat(result.id()).isEqualTo(901L);
    assertThat(result.weddingBookingId()).isEqualTo(51L);
    assertThat(result.coupleName()).isEqualTo("Minh & Lan");
    assertThat(result.menuItems()).hasSize(1);
    assertThat(result.services()).hasSize(1);
    assertThat(result.incidentalReceipts()).hasSize(1);
    assertThat(result.finalAmount()).isEqualByComparingTo("60392500.00");
  }

  @Test
  void shouldRejectMissingInvoice() {
    when(invoiceQueryPort.findInvoiceById(901L)).thenReturn(Optional.empty());

    GetInvoiceDetailService service =
        new GetInvoiceDetailService(
            invoiceQueryPort, weddingBookingQueryPort, incidentalReceiptQueryPort);

    assertThatThrownBy(() -> service.getInvoiceDetail(901L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Invoice not found with id: 901");
  }

  private Invoice createInvoice() {
    return new Invoice(
        901L,
        51L,
        99L,
        Instant.parse("2026-08-16T15:00:00Z"),
        new BigDecimal("100000000.00"),
        new BigDecimal("3000000.00"),
        new BigDecimal("5000000.00"),
        new BigDecimal("1500000.00"),
        new BigDecimal("50000000.00"),
        new BigDecimal("892500.00"),
        new BigDecimal("60392500.00"),
        "Thanh toan lan cuoi");
  }

  private WeddingBooking createWeddingBooking() {
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
        WeddingBookingStatus.DA_THANH_TOAN,
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

  private IncidentalReceipt createIncidentalReceipt() {
    return new IncidentalReceipt(
        701L,
        51L,
        99L,
        Instant.parse("2026-08-15T12:00:00Z"),
        new BigDecimal("1500000.00"),
        "Them 1 dich vu",
        List.of(
            new IncidentalReceiptItem(
                801L,
                31L,
                "MC su kien",
                "gio",
                1,
                new BigDecimal("1500000.00"),
                new BigDecimal("1500000.00"),
                null)));
  }
}
