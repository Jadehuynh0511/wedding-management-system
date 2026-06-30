package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.ResourceInUseException;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallBookingReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Hall;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallStatus;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteHallServiceTest {

  @Mock private HallQueryPort hallQueryPort;

  @Mock private HallCommandPort hallCommandPort;

  @Mock private HallBookingReferenceQueryPort hallBookingReferenceQueryPort;

  @Test
  void shouldDeleteHallWhenNoWeddingBookingReferencesIt() {
    HallType hallType = new HallType(3L, "Emerald", new BigDecimal("5500000.00"), null);
    Hall hall =
        new Hall(
            9L,
            hallType,
            "Grand Palace",
            420,
            new BigDecimal("6200000.00"),
            HallStatus.TRONG,
            null);

    when(hallQueryPort.findHallById(9L)).thenReturn(Optional.of(hall));
    when(hallBookingReferenceQueryPort.existsWeddingBookingByHallId(9L)).thenReturn(false);

    DeleteHallService deleteHallService =
        new DeleteHallService(hallQueryPort, hallCommandPort, hallBookingReferenceQueryPort);

    deleteHallService.deleteHall(9L);

    verify(hallCommandPort).deleteHallById(9L);
  }

  @Test
  void shouldRejectDeleteWhenHallIsUsedByWeddingBookings() {
    HallType hallType = new HallType(3L, "Emerald", new BigDecimal("5500000.00"), null);
    Hall hall =
        new Hall(
            9L,
            hallType,
            "Grand Palace",
            420,
            new BigDecimal("6200000.00"),
            HallStatus.TRONG,
            null);

    when(hallQueryPort.findHallById(9L)).thenReturn(Optional.of(hall));
    when(hallBookingReferenceQueryPort.existsWeddingBookingByHallId(9L)).thenReturn(true);

    DeleteHallService deleteHallService =
        new DeleteHallService(hallQueryPort, hallCommandPort, hallBookingReferenceQueryPort);

    assertThatThrownBy(() -> deleteHallService.deleteHall(9L))
        .isInstanceOf(ResourceInUseException.class)
        .hasMessage("Hall is currently used by existing wedding bookings and cannot be deleted.");

    verify(hallCommandPort, never()).deleteHallById(9L);
  }
}
