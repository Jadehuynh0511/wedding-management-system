package com.uit.weddingmanagement.modules.booking.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Hall;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallStatus;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListAvailableHallsServiceTest {

  @Mock private HallQueryPort hallQueryPort;

  @Mock private ShiftQueryPort shiftQueryPort;

  @Mock private WeddingBookingQueryPort weddingBookingQueryPort;

  @Test
  void shouldReturnOnlyHallsThatAreNotBookedAndNotUnderMaintenance() {
    HallType hallType = new HallType(3L, "Premium", new BigDecimal("4500000.00"), "VIP");
    Shift shift = new Shift(2L, "Evening", LocalTime.of(17, 0), LocalTime.of(21, 0), null);

    when(shiftQueryPort.findShiftById(2L)).thenReturn(Optional.of(shift));
    when(hallQueryPort.findAllHalls())
        .thenReturn(
            List.of(
                new Hall(
                    7L,
                    hallType,
                    "Sunrise Hall",
                    350,
                    new BigDecimal("5000000.00"),
                    HallStatus.TRONG,
                    null),
                new Hall(
                    8L,
                    hallType,
                    "Maintenance Hall",
                    250,
                    new BigDecimal("4800000.00"),
                    HallStatus.BAO_TRI,
                    null),
                new Hall(
                    9L,
                    hallType,
                    "Booked Hall",
                    250,
                    new BigDecimal("4700000.00"),
                    HallStatus.TRONG,
                    null)));
    when(weddingBookingQueryPort.findBookedHallIdsByCelebrationDateAndShiftId(
            LocalDate.of(2026, 8, 15), 2L))
        .thenReturn(Set.of(9L));

    ListAvailableHallsService listAvailableHallsService =
        new ListAvailableHallsService(hallQueryPort, shiftQueryPort, weddingBookingQueryPort);

    var results = listAvailableHallsService.listAvailableHalls(LocalDate.of(2026, 8, 15), 2L);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().id()).isEqualTo(7L);
    assertThat(results.getFirst().hallName()).isEqualTo("Sunrise Hall");
  }
}
