package com.uit.weddingmanagement.modules.booking.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateIncidentalReceiptCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateIncidentalReceiptItemCommand;
import com.uit.weddingmanagement.modules.booking.application.port.out.IncidentalReceiptCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingMenuItem;
import com.uit.weddingmanagement.modules.booking.domain.model.DepositReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceiptItem;
import com.uit.weddingmanagement.modules.booking.domain.model.PaymentMethod;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
class CreateIncidentalReceiptServiceTest {

  @Mock private WeddingBookingQueryPort weddingBookingQueryPort;

  @Mock private IncidentalReceiptCommandPort incidentalReceiptCommandPort;

  @Mock private ServiceItemQueryPort serviceItemQueryPort;

  @Mock private CurrentUserPort currentUserPort;

  @Captor private ArgumentCaptor<IncidentalReceipt> incidentalReceiptCaptor;

  @Test
  void shouldCreateIncidentalReceiptWhenBookingIsEligible() {
    WeddingBooking weddingBooking = createWeddingBooking(51L, WeddingBookingStatus.DA_XAC_NHAN);
    ServiceItem photobooth = createServiceItem(21L, "Photobooth", "goi", "4500000.00", true);
    ServiceItem dessertBar = createServiceItem(22L, "Dessert bar", "set", "2500000.00", true);
    AuthenticatedUser currentUser =
        new AuthenticatedUser(
            99L,
            "staff.billing",
            "Billing Staff",
            3L,
            "STAFF",
            Set.of("INCIDENTAL_RECEIPT_CREATE"));

    when(weddingBookingQueryPort.findWeddingBookingByIdForUpdate(51L))
        .thenReturn(Optional.of(weddingBooking));
    when(serviceItemQueryPort.findServiceItemById(21L)).thenReturn(Optional.of(photobooth));
    when(serviceItemQueryPort.findServiceItemById(22L)).thenReturn(Optional.of(dessertBar));
    when(currentUserPort.getCurrentUser()).thenReturn(currentUser);
    when(incidentalReceiptCommandPort.saveIncidentalReceipt(any(IncidentalReceipt.class)))
        .thenAnswer(
            invocation -> {
              IncidentalReceipt input = invocation.getArgument(0, IncidentalReceipt.class);

              return new IncidentalReceipt(
                  701L,
                  input.weddingBookingId(),
                  input.userId(),
                  input.recordedAt(),
                  input.totalAmount(),
                  input.notes(),
                  List.of(
                      new IncidentalReceiptItem(
                          801L,
                          input.items().get(0).serviceId(),
                          input.items().get(0).serviceName(),
                          input.items().get(0).unitName(),
                          input.items().get(0).quantity(),
                          input.items().get(0).appliedUnitPrice(),
                          input.items().get(0).lineTotal(),
                          input.items().get(0).notes()),
                      new IncidentalReceiptItem(
                          802L,
                          input.items().get(1).serviceId(),
                          input.items().get(1).serviceName(),
                          input.items().get(1).unitName(),
                          input.items().get(1).quantity(),
                          input.items().get(1).appliedUnitPrice(),
                          input.items().get(1).lineTotal(),
                          input.items().get(1).notes())));
            });

    CreateIncidentalReceiptService createIncidentalReceiptService =
        new CreateIncidentalReceiptService(
            weddingBookingQueryPort,
            incidentalReceiptCommandPort,
            serviceItemQueryPort,
            currentUserPort);

    var result =
        createIncidentalReceiptService.createIncidentalReceipt(
            51L,
            new CreateIncidentalReceiptCommand(
                "Them dich vu truoc gio khai tiec",
                List.of(
                    new CreateIncidentalReceiptItemCommand(21L, 1, null),
                    new CreateIncidentalReceiptItemCommand(22L, 2, "Ban premium"))));

    assertThat(result.id()).isEqualTo(701L);
    assertThat(result.weddingBookingId()).isEqualTo(51L);
    assertThat(result.userId()).isEqualTo(99L);
    assertThat(result.totalAmount()).isEqualByComparingTo("9500000.00");
    assertThat(result.items()).hasSize(2);
    assertThat(result.items().get(1).lineTotal()).isEqualByComparingTo("5000000.00");

    verify(incidentalReceiptCommandPort).saveIncidentalReceipt(incidentalReceiptCaptor.capture());
    IncidentalReceipt savedIncidentalReceipt = incidentalReceiptCaptor.getValue();
    assertThat(savedIncidentalReceipt.id()).isNull();
    assertThat(savedIncidentalReceipt.weddingBookingId()).isEqualTo(51L);
    assertThat(savedIncidentalReceipt.userId()).isEqualTo(99L);
    assertThat(savedIncidentalReceipt.totalAmount()).isEqualByComparingTo("9500000.00");
    assertThat(savedIncidentalReceipt.items()).hasSize(2);
    assertThat(savedIncidentalReceipt.items().getFirst().appliedUnitPrice())
        .isEqualByComparingTo("4500000.00");
    assertThat(savedIncidentalReceipt.items().getLast().lineTotal())
        .isEqualByComparingTo("5000000.00");
  }

  @Test
  void shouldRejectWhenWeddingBookingIsAlreadyPaid() {
    when(weddingBookingQueryPort.findWeddingBookingByIdForUpdate(51L))
        .thenReturn(Optional.of(createWeddingBooking(51L, WeddingBookingStatus.DA_THANH_TOAN)));

    CreateIncidentalReceiptService createIncidentalReceiptService =
        new CreateIncidentalReceiptService(
            weddingBookingQueryPort,
            incidentalReceiptCommandPort,
            serviceItemQueryPort,
            currentUserPort);

    assertThatThrownBy(
            () ->
                createIncidentalReceiptService.createIncidentalReceipt(
                    51L,
                    new CreateIncidentalReceiptCommand(
                        null, List.of(new CreateIncidentalReceiptItemCommand(21L, 1, null)))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot create incidental receipt for a fully paid wedding booking.");

    verify(incidentalReceiptCommandPort, never()).saveIncidentalReceipt(any(IncidentalReceipt.class));
  }

  @Test
  void shouldRejectWhenWeddingBookingIsCancelled() {
    when(weddingBookingQueryPort.findWeddingBookingByIdForUpdate(51L))
        .thenReturn(Optional.of(createWeddingBooking(51L, WeddingBookingStatus.DA_HUY)));

    CreateIncidentalReceiptService createIncidentalReceiptService =
        new CreateIncidentalReceiptService(
            weddingBookingQueryPort,
            incidentalReceiptCommandPort,
            serviceItemQueryPort,
            currentUserPort);

    assertThatThrownBy(
            () ->
                createIncidentalReceiptService.createIncidentalReceipt(
                    51L,
                    new CreateIncidentalReceiptCommand(
                        null, List.of(new CreateIncidentalReceiptItemCommand(21L, 1, null)))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot create incidental receipt for a cancelled wedding booking.");

    verify(incidentalReceiptCommandPort, never()).saveIncidentalReceipt(any(IncidentalReceipt.class));
  }

  @Test
  void shouldRejectWhenServiceItemIsInactive() {
    when(weddingBookingQueryPort.findWeddingBookingByIdForUpdate(51L))
        .thenReturn(Optional.of(createWeddingBooking(51L, WeddingBookingStatus.DA_XAC_NHAN)));
    when(serviceItemQueryPort.findServiceItemById(21L))
        .thenReturn(Optional.of(createServiceItem(21L, "Photobooth", "goi", "4500000.00", false)));

    CreateIncidentalReceiptService createIncidentalReceiptService =
        new CreateIncidentalReceiptService(
            weddingBookingQueryPort,
            incidentalReceiptCommandPort,
            serviceItemQueryPort,
            currentUserPort);

    assertThatThrownBy(
            () ->
                createIncidentalReceiptService.createIncidentalReceipt(
                    51L,
                    new CreateIncidentalReceiptCommand(
                        null, List.of(new CreateIncidentalReceiptItemCommand(21L, 1, null)))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Service item is not active for incidental receipt: Photobooth");

    verify(incidentalReceiptCommandPort, never()).saveIncidentalReceipt(any(IncidentalReceipt.class));
  }

  private WeddingBooking createWeddingBooking(Long bookingId, WeddingBookingStatus status) {
    return new WeddingBooking(
        bookingId,
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
        List.of(),
        new DepositReceipt(
            300L,
            bookingId,
            88L,
            Instant.parse("2026-06-01T08:00:00Z"),
            new BigDecimal("50000000.00"),
            PaymentMethod.CHUYEN_KHOAN,
            null));
  }

  private ServiceItem createServiceItem(
      Long serviceId, String serviceName, String unitName, String currentPrice, boolean active) {
    return new ServiceItem(
        serviceId,
        serviceName,
        "Add-on",
        unitName,
        new BigDecimal(currentPrice),
        Instant.parse("2026-05-01T00:00:00Z"),
        active ? ServiceItemStatus.HOAT_DONG : ServiceItemStatus.NGUNG_HOAT_DONG,
        null);
  }
}
