package com.uit.weddingmanagement.modules.booking.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.booking.application.port.out.IncidentalReceiptQueryPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingMenuItem;
import com.uit.weddingmanagement.modules.booking.domain.model.DepositReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceiptItem;
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
class ListIncidentalReceiptsServiceTest {

  @Mock private WeddingBookingQueryPort weddingBookingQueryPort;
  @Mock private IncidentalReceiptQueryPort incidentalReceiptQueryPort;

  @Test
  void shouldReturnIncidentalReceiptsWhenBookingExists() {
    when(weddingBookingQueryPort.findWeddingBookingById(44L)).thenReturn(Optional.of(booking()));
    when(incidentalReceiptQueryPort.findIncidentalReceiptsByWeddingBookingId(44L))
        .thenReturn(
            List.of(
                new IncidentalReceipt(
                    91L,
                    44L,
                    7L,
                    Instant.parse("2026-08-15T12:00:00Z"),
                    new BigDecimal("1500000.00"),
                    "Ban tiec VIP",
                    List.of(
                        IncidentalReceiptItem.create(
                            11L,
                            "Trang tri cong chao",
                            "goi",
                            1,
                            new BigDecimal("1500000.00"),
                            null)))));

    ListIncidentalReceiptsService listIncidentalReceiptsService =
        new ListIncidentalReceiptsService(weddingBookingQueryPort, incidentalReceiptQueryPort);

    var result = listIncidentalReceiptsService.listIncidentalReceipts(44L);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().id()).isEqualTo(91L);
    assertThat(result.getFirst().totalAmount()).isEqualByComparingTo("1500000.00");
    assertThat(result.getFirst().items()).hasSize(1);
    assertThat(result.getFirst().items().getFirst().serviceName()).isEqualTo("Trang tri cong chao");
  }

  @Test
  void shouldReturnEmptyListWhenBookingExistsWithoutIncidentals() {
    when(weddingBookingQueryPort.findWeddingBookingById(44L)).thenReturn(Optional.of(booking()));
    when(incidentalReceiptQueryPort.findIncidentalReceiptsByWeddingBookingId(44L))
        .thenReturn(List.of());

    ListIncidentalReceiptsService listIncidentalReceiptsService =
        new ListIncidentalReceiptsService(weddingBookingQueryPort, incidentalReceiptQueryPort);

    var result = listIncidentalReceiptsService.listIncidentalReceipts(44L);

    assertThat(result).isEmpty();
    verify(incidentalReceiptQueryPort).findIncidentalReceiptsByWeddingBookingId(44L);
  }

  @Test
  void shouldThrowNotFoundWhenBookingDoesNotExist() {
    when(weddingBookingQueryPort.findWeddingBookingById(44L)).thenReturn(Optional.empty());

    ListIncidentalReceiptsService listIncidentalReceiptsService =
        new ListIncidentalReceiptsService(weddingBookingQueryPort, incidentalReceiptQueryPort);

    assertThatThrownBy(() -> listIncidentalReceiptsService.listIncidentalReceipts(44L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Wedding booking not found with id: 44");
  }

  private WeddingBooking booking() {
    return new WeddingBooking(
        44L,
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
        WeddingBookingStatus.DA_XAC_NHAN,
        "Ban gan san khau",
        List.of(
            BookingMenuItem.create(
                21L, "Soup cua", 1, new BigDecimal("150000.00"), null)),
        List.of(),
        new DepositReceipt(
            55L,
            44L,
            99L,
            Instant.parse("2026-06-01T08:00:00Z"),
            new BigDecimal("50000000.00"),
            PaymentMethod.CHUYEN_KHOAN,
            null));
  }
}
