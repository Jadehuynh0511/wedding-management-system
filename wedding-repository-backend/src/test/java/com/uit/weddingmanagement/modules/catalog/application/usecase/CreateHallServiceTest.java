package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateHallCommand;
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
class CreateHallServiceTest {

  @Mock private HallQueryPort hallQueryPort;

  @Mock private HallTypeQueryPort hallTypeQueryPort;

  @Mock private HallCommandPort hallCommandPort;

  @Captor private ArgumentCaptor<Hall> hallCaptor;

  @Test
  void shouldCreateHallWhenRequestIsValid() {
    HallType hallType = new HallType(3L, "Emerald", new BigDecimal("5500000.00"), "Premium");

    when(hallTypeQueryPort.findHallTypeById(3L)).thenReturn(Optional.of(hallType));
    when(hallQueryPort.existsHallByName("Grand Palace")).thenReturn(false);
    when(hallCommandPort.saveHall(any(Hall.class)))
        .thenReturn(
            new Hall(
                12L,
                hallType,
                "Grand Palace",
                420,
                new BigDecimal("6200000.00"),
                HallStatus.TRONG,
                "Sảnh trung tâm"));

    CreateHallService createHallService =
        new CreateHallService(hallQueryPort, hallTypeQueryPort, hallCommandPort);

    var result =
        createHallService.createHall(
            new CreateHallCommand(
                3L,
                "  Grand   Palace  ",
                420,
                new BigDecimal("6200000.00"),
                null,
                "  Sảnh trung tâm  "));

    assertThat(result.id()).isEqualTo(12L);
    assertThat(result.hallTypeId()).isEqualTo(3L);
    assertThat(result.hallName()).isEqualTo("Grand Palace");
    assertThat(result.tablePrice()).isEqualByComparingTo("6200000.00");
    assertThat(result.status()).isEqualTo(HallStatus.TRONG);

    verify(hallCommandPort).saveHall(hallCaptor.capture());
    Hall savedHall = hallCaptor.getValue();
    assertThat(savedHall.id()).isNull();
    assertThat(savedHall.hallType()).isEqualTo(hallType);
    assertThat(savedHall.hallName()).isEqualTo("Grand Palace");
    assertThat(savedHall.maxCapacity()).isEqualTo(420);
    assertThat(savedHall.tablePrice()).isEqualByComparingTo("6200000.00");
    assertThat(savedHall.status()).isEqualTo(HallStatus.TRONG);
    assertThat(savedHall.description()).isEqualTo("Sảnh trung tâm");
  }

  @Test
  void shouldRejectDuplicateHallNameIgnoringCase() {
    HallType hallType = new HallType(3L, "Emerald", new BigDecimal("5500000.00"), "Premium");

    when(hallTypeQueryPort.findHallTypeById(3L)).thenReturn(Optional.of(hallType));
    when(hallQueryPort.existsHallByName("grand palace")).thenReturn(true);

    CreateHallService createHallService =
        new CreateHallService(hallQueryPort, hallTypeQueryPort, hallCommandPort);

    assertThatThrownBy(
            () ->
                createHallService.createHall(
                    new CreateHallCommand(
                        3L,
                        " grand palace ",
                        400,
                        new BigDecimal("5600000.00"),
                        HallStatus.TRONG,
                        null)))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Hall name already exists: grand palace");

    verify(hallCommandPort, never()).saveHall(any(Hall.class));
  }

  @Test
  void shouldRejectWhenTablePriceIsBelowHallTypeMinimum() {
    HallType hallType = new HallType(3L, "Emerald", new BigDecimal("5500000.00"), "Premium");

    when(hallTypeQueryPort.findHallTypeById(3L)).thenReturn(Optional.of(hallType));

    CreateHallService createHallService =
        new CreateHallService(hallQueryPort, hallTypeQueryPort, hallCommandPort);

    assertThatThrownBy(
            () ->
                createHallService.createHall(
                    new CreateHallCommand(
                        3L,
                        "Grand Palace",
                        400,
                        new BigDecimal("5400000.00"),
                        HallStatus.TRONG,
                        null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Table price must be greater than or equal to minimum table price of the hall type.");

    verify(hallCommandPort, never()).saveHall(any(Hall.class));
  }
}
