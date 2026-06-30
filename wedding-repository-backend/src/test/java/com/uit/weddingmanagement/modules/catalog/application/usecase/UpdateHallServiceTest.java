package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateHallCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Hall;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallStatus;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateHallServiceTest {

  @Mock private HallQueryPort hallQueryPort;

  @Mock private HallTypeQueryPort hallTypeQueryPort;

  @Mock private HallCommandPort hallCommandPort;

  @Captor private ArgumentCaptor<Hall> hallCaptor;

  @Test
  void shouldUpdateHallWhenRequestIsValid() {
    HallType hallType = new HallType(5L, "Diamond", new BigDecimal("6800000.00"), "VIP");
    Hall existingHall =
        new Hall(
            21L,
            hallType,
            "Royal Garden",
            500,
            new BigDecimal("7000000.00"),
            HallStatus.TRONG,
            "Cũ");

    when(hallQueryPort.findHallById(21L)).thenReturn(Optional.of(existingHall));
    when(hallTypeQueryPort.findHallTypeById(5L)).thenReturn(Optional.of(hallType));
    when(hallQueryPort.existsHallByNameAndIdNot("Royal Garden Plus", 21L)).thenReturn(false);
    when(hallCommandPort.saveHall(any(Hall.class)))
        .thenReturn(
            new Hall(
                21L,
                hallType,
                "Royal Garden Plus",
                550,
                new BigDecimal("7200000.00"),
                HallStatus.DANG_DUNG,
                "Nâng cấp"));

    UpdateHallService updateHallService =
        new UpdateHallService(hallQueryPort, hallTypeQueryPort, hallCommandPort);

    var result =
        updateHallService.updateHall(
            21L,
            new UpdateHallCommand(
                5L,
                "  Royal   Garden Plus  ",
                550,
                new BigDecimal("7200000.00"),
                HallStatus.DANG_DUNG,
                "  Nâng cấp  "));

    assertThat(result.id()).isEqualTo(21L);
    assertThat(result.hallName()).isEqualTo("Royal Garden Plus");
    assertThat(result.status()).isEqualTo(HallStatus.DANG_DUNG);

    verify(hallCommandPort).saveHall(hallCaptor.capture());
    Hall savedHall = hallCaptor.getValue();
    assertThat(savedHall.id()).isEqualTo(21L);
    assertThat(savedHall.hallName()).isEqualTo("Royal Garden Plus");
    assertThat(savedHall.maxCapacity()).isEqualTo(550);
    assertThat(savedHall.description()).isEqualTo("Nâng cấp");
  }

  @Test
  void shouldRejectDuplicateHallNameWhenUpdating() {
    HallType hallType = new HallType(5L, "Diamond", new BigDecimal("6800000.00"), "VIP");
    Hall existingHall =
        new Hall(
            21L,
            hallType,
            "Royal Garden",
            500,
            new BigDecimal("7000000.00"),
            HallStatus.TRONG,
            null);

    when(hallQueryPort.findHallById(21L)).thenReturn(Optional.of(existingHall));
    when(hallTypeQueryPort.findHallTypeById(5L)).thenReturn(Optional.of(hallType));
    when(hallQueryPort.existsHallByNameAndIdNot("royal garden", 21L)).thenReturn(true);

    UpdateHallService updateHallService =
        new UpdateHallService(hallQueryPort, hallTypeQueryPort, hallCommandPort);

    assertThatThrownBy(
            () ->
                updateHallService.updateHall(
                    21L,
                    new UpdateHallCommand(
                        5L,
                        " royal garden ",
                        500,
                        new BigDecimal("7000000.00"),
                        HallStatus.TRONG,
                        null)))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Hall name already exists: royal garden");

    verify(hallCommandPort, never()).saveHall(any(Hall.class));
  }
}
