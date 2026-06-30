package com.uit.weddingmanagement.modules.booking.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateDepositReceiptCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateWeddingBookingCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateWeddingBookingMenuItemCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateWeddingBookingServiceCommand;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingMenuItem;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingService;
import com.uit.weddingmanagement.modules.booking.domain.model.DepositReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.PaymentMethod;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Hall;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallStatus;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItemStatus;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateWeddingBookingServiceTest {

  @Mock private WeddingBookingQueryPort weddingBookingQueryPort;

  @Mock private WeddingBookingCommandPort weddingBookingCommandPort;

  @Mock private HallQueryPort hallQueryPort;

  @Mock private ShiftQueryPort shiftQueryPort;

  @Mock private MenuItemQueryPort menuItemQueryPort;

  @Mock private ServiceItemQueryPort serviceItemQueryPort;

  @Mock private SystemParameterQueryPort systemParameterQueryPort;

  @Mock private CurrentUserPort currentUserPort;

  @Captor private ArgumentCaptor<WeddingBooking> weddingBookingCaptor;

  @Test
  void shouldCreateWeddingBookingWhenRequestIsValid() {
    Hall hall = createHall(HallStatus.TRONG);
    Shift shift = new Shift(2L, "Evening", LocalTime.of(17, 0), LocalTime.of(21, 0), null);
    MenuItem menuItem =
        new MenuItem(
            11L,
            "Soup cua",
            "Khai vi",
            new BigDecimal("150000.00"),
            MenuItemStatus.CON,
            null);
    ServiceItem serviceItem =
        new ServiceItem(
            21L,
            "Trang tri hoa tuoi",
            "Trang tri",
            "goi",
            new BigDecimal("5000000.00"),
            Instant.parse("2026-01-01T00:00:00Z"),
            ServiceItemStatus.HOAT_DONG,
            null);
    SystemParameter systemParameter =
        new SystemParameter(
            SystemParameter.SINGLETON_ID,
            new BigDecimal("50.00"),
            true,
            new BigDecimal("1.00"),
            15,
            new BigDecimal("50.00"));
    AuthenticatedUser currentUser =
        new AuthenticatedUser(
            99L, "staff.booking", "Booking Staff", 2L, "STAFF", Set.of("WEDDING_BOOKING_CREATE"));

    when(hallQueryPort.findHallById(7L)).thenReturn(Optional.of(hall));
    when(shiftQueryPort.findShiftById(2L)).thenReturn(Optional.of(shift));
    when(weddingBookingQueryPort.existsActiveBookingByHallIdAndShiftIdAndCelebrationDate(
            7L, 2L, LocalDate.of(2026, 8, 15)))
        .thenReturn(false);
    when(menuItemQueryPort.findMenuItemById(11L)).thenReturn(Optional.of(menuItem));
    when(serviceItemQueryPort.findServiceItemById(21L)).thenReturn(Optional.of(serviceItem));
    when(systemParameterQueryPort.getSystemParameter()).thenReturn(Optional.of(systemParameter));
    when(currentUserPort.getCurrentUser()).thenReturn(currentUser);
    when(weddingBookingCommandPort.saveWeddingBooking(any(WeddingBooking.class)))
        .thenAnswer(
            invocation -> {
              WeddingBooking input = invocation.getArgument(0, WeddingBooking.class);
              return new WeddingBooking(
                  44L,
                  input.hallId(),
                  input.hallName(),
                  input.shiftId(),
                  input.shiftName(),
                  input.groomName(),
                  input.brideName(),
                  input.groomPhoneNumber(),
                  input.bridePhoneNumber(),
                  input.bookingDate(),
                  input.celebrationDate(),
                  input.tableCount(),
                  input.reservedTableCount(),
                  input.tablePrice(),
                  input.status(),
                  input.notes(),
                  input.menuItems().stream()
                      .map(
                          item ->
                              new BookingMenuItem(
                                  100L,
                                  item.menuItemId(),
                                  item.menuItemName(),
                                  item.quantity(),
                                  item.priceSnapshot(),
                                  item.lineTotal(),
                                  item.notes()))
                      .toList(),
                  input.services().stream()
                      .map(
                          service ->
                              new BookingService(
                                  200L,
                                  service.serviceId(),
                                  service.serviceName(),
                                  service.unitName(),
                                  service.quantity(),
                                  service.priceSnapshot(),
                                  service.lineTotal(),
                                  service.notes()))
                      .toList(),
                  new DepositReceipt(
                      300L,
                      44L,
                      input.depositReceipt().userId(),
                      input.depositReceipt().receivedAt(),
                      input.depositReceipt().amount(),
                      input.depositReceipt().paymentMethod(),
                      input.depositReceipt().notes()));
            });

    CreateWeddingBookingService createWeddingBookingService =
        new CreateWeddingBookingService(
            weddingBookingQueryPort,
            weddingBookingCommandPort,
            hallQueryPort,
            shiftQueryPort,
            menuItemQueryPort,
            serviceItemQueryPort,
            systemParameterQueryPort,
            currentUserPort);

    var result =
        createWeddingBookingService.createWeddingBooking(
            new CreateWeddingBookingCommand(
                7L,
                2L,
                "Minh",
                "Lan",
                "0909000111",
                "0909000222",
                LocalDate.of(2026, 8, 15),
                20,
                2,
                "Ban gan san khau",
                java.util.List.of(new CreateWeddingBookingMenuItemCommand(11L, 1, "It cay")),
                java.util.List.of(new CreateWeddingBookingServiceCommand(21L, 1, null)),
                new CreateDepositReceiptCommand(
                    new BigDecimal("50000000.00"), PaymentMethod.CHUYEN_KHOAN, "Coc lan 1")));

    assertThat(result.id()).isEqualTo(44L);
    assertThat(result.status()).isEqualTo(WeddingBookingStatus.DA_XAC_NHAN);
    assertThat(result.hallTotalAmount()).isEqualByComparingTo("100000000.00");
    assertThat(result.depositReceipt().amount()).isEqualByComparingTo("50000000.00");
    assertThat(result.depositReceipt().paymentMethod()).isEqualTo(PaymentMethod.CHUYEN_KHOAN);
    assertThat(result.menuItems()).hasSize(1);
    assertThat(result.services()).hasSize(1);

    verify(weddingBookingCommandPort).saveWeddingBooking(weddingBookingCaptor.capture());
    WeddingBooking savedWeddingBooking = weddingBookingCaptor.getValue();
    assertThat(savedWeddingBooking.id()).isNull();
    assertThat(savedWeddingBooking.hallId()).isEqualTo(7L);
    assertThat(savedWeddingBooking.shiftId()).isEqualTo(2L);
    assertThat(savedWeddingBooking.bookingDate()).isEqualTo(LocalDate.now(ZoneOffset.UTC));
    assertThat(savedWeddingBooking.tablePrice()).isEqualByComparingTo("5000000.00");
    assertThat(savedWeddingBooking.depositReceipt().userId()).isEqualTo(99L);
    assertThat(savedWeddingBooking.menuItems().getFirst().lineTotal()).isEqualByComparingTo("150000.00");
  }

  @Test
  void shouldRejectWhenSlotAlreadyHasActiveWeddingBooking() {
    Hall hall = createHall(HallStatus.TRONG);
    Shift shift = new Shift(2L, "Evening", LocalTime.of(17, 0), LocalTime.of(21, 0), null);

    when(hallQueryPort.findHallById(7L)).thenReturn(Optional.of(hall));
    when(shiftQueryPort.findShiftById(2L)).thenReturn(Optional.of(shift));
    when(weddingBookingQueryPort.existsActiveBookingByHallIdAndShiftIdAndCelebrationDate(
            7L, 2L, LocalDate.of(2026, 8, 15)))
        .thenReturn(true);

    CreateWeddingBookingService createWeddingBookingService =
        new CreateWeddingBookingService(
            weddingBookingQueryPort,
            weddingBookingCommandPort,
            hallQueryPort,
            shiftQueryPort,
            menuItemQueryPort,
            serviceItemQueryPort,
            systemParameterQueryPort,
            currentUserPort);

    assertThatThrownBy(
            () ->
                createWeddingBookingService.createWeddingBooking(
                    new CreateWeddingBookingCommand(
                        7L,
                        2L,
                        "Minh",
                        "Lan",
                        null,
                        "0909000222",
                        LocalDate.of(2026, 8, 15),
                        20,
                        0,
                        null,
                        java.util.List.of(new CreateWeddingBookingMenuItemCommand(11L, 1, null)),
                        java.util.List.of(),
                        new CreateDepositReceiptCommand(
                            new BigDecimal("50000000.00"), PaymentMethod.TIEN_MAT, null))))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Hall is already booked for the selected celebration date and shift.");

    verify(weddingBookingCommandPort, never()).saveWeddingBooking(any(WeddingBooking.class));
  }

  @Test
  void shouldRejectWhenDepositAmountIsBelowConfiguredMinimum() {
    Hall hall = createHall(HallStatus.TRONG);
    Shift shift = new Shift(2L, "Evening", LocalTime.of(17, 0), LocalTime.of(21, 0), null);
    MenuItem menuItem =
        new MenuItem(
            11L,
            "Soup cua",
            "Khai vi",
            new BigDecimal("150000.00"),
            MenuItemStatus.CON,
            null);
    SystemParameter systemParameter =
        new SystemParameter(
            SystemParameter.SINGLETON_ID,
            new BigDecimal("50.00"),
            true,
            new BigDecimal("1.00"),
            15,
            new BigDecimal("50.00"));

    when(hallQueryPort.findHallById(7L)).thenReturn(Optional.of(hall));
    when(shiftQueryPort.findShiftById(2L)).thenReturn(Optional.of(shift));
    when(weddingBookingQueryPort.existsActiveBookingByHallIdAndShiftIdAndCelebrationDate(
            7L, 2L, LocalDate.of(2026, 8, 15)))
        .thenReturn(false);
    when(menuItemQueryPort.findMenuItemById(11L)).thenReturn(Optional.of(menuItem));
    when(systemParameterQueryPort.getSystemParameter()).thenReturn(Optional.of(systemParameter));
    when(currentUserPort.getCurrentUser())
        .thenReturn(
            new AuthenticatedUser(99L, "staff.booking", "Booking Staff", 2L, "STAFF", Set.of()));

    CreateWeddingBookingService createWeddingBookingService =
        new CreateWeddingBookingService(
            weddingBookingQueryPort,
            weddingBookingCommandPort,
            hallQueryPort,
            shiftQueryPort,
            menuItemQueryPort,
            serviceItemQueryPort,
            systemParameterQueryPort,
            currentUserPort);

    assertThatThrownBy(
            () ->
                createWeddingBookingService.createWeddingBooking(
                    new CreateWeddingBookingCommand(
                        7L,
                        2L,
                        "Minh",
                        "Lan",
                        null,
                        "0909000222",
                        LocalDate.of(2026, 8, 15),
                        20,
                        0,
                        null,
                        java.util.List.of(new CreateWeddingBookingMenuItemCommand(11L, 1, null)),
                        java.util.List.of(),
                        new CreateDepositReceiptCommand(
                            new BigDecimal("49999999.99"), PaymentMethod.TIEN_MAT, null))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Deposit amount must be greater than or equal to minimum required amount of 50000000.00.");

    verify(weddingBookingCommandPort, never()).saveWeddingBooking(any(WeddingBooking.class));
  }

  private Hall createHall(HallStatus hallStatus) {
    HallType hallType = new HallType(3L, "Premium", new BigDecimal("4500000.00"), "VIP");

    return new Hall(
        7L,
        hallType,
        "Sunrise Hall",
        350,
        new BigDecimal("5000000.00"),
        hallStatus,
        "Sanh tang 2");
  }
}
