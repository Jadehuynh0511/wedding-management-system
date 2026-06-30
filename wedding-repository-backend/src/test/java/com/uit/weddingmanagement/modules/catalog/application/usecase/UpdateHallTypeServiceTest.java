package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateHallTypeCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeQueryPort;
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
class UpdateHallTypeServiceTest {

  @Mock private HallTypeQueryPort hallTypeQueryPort;

  @Mock private HallTypeCommandPort hallTypeCommandPort;

  @Captor private ArgumentCaptor<HallType> hallTypeCaptor;

  @Test
  void shouldUpdateHallTypeWhenDataIsValid() {
    HallType existingHallType =
        new HallType(5L, "Ruby", new BigDecimal("3200000.00"), "Old description");

    when(hallTypeQueryPort.findHallTypeById(5L)).thenReturn(Optional.of(existingHallType));
    when(hallTypeQueryPort.existsHallTypeByNameAndIdNot("Ruby Palace", 5L)).thenReturn(false);
    when(hallTypeCommandPort.saveHallType(org.mockito.ArgumentMatchers.any(HallType.class)))
        .thenReturn(
            new HallType(5L, "Ruby Palace", new BigDecimal("5000000.00"), "New description"));

    UpdateHallTypeService updateHallTypeService =
        new UpdateHallTypeService(hallTypeQueryPort, hallTypeCommandPort);

    var result =
        updateHallTypeService.updateHallType(
            5L,
            new UpdateHallTypeCommand(
                " Ruby Palace ", new BigDecimal("5000000.00"), " New description "));

    assertThat(result.id()).isEqualTo(5L);
    assertThat(result.hallTypeName()).isEqualTo("Ruby Palace");
    assertThat(result.minimumTablePrice()).isEqualByComparingTo("5000000.00");
    assertThat(result.description()).isEqualTo("New description");

    verify(hallTypeCommandPort).saveHallType(hallTypeCaptor.capture());
    HallType savedHallType = hallTypeCaptor.getValue();
    assertThat(savedHallType.id()).isEqualTo(5L);
    assertThat(savedHallType.hallTypeName()).isEqualTo("Ruby Palace");
    assertThat(savedHallType.minimumTablePrice()).isEqualByComparingTo("5000000.00");
    assertThat(savedHallType.description()).isEqualTo("New description");
  }

  @Test
  void shouldRejectDuplicateNameOwnedByAnotherHallType() {
    HallType existingHallType = new HallType(5L, "Ruby", new BigDecimal("3200000.00"), null);

    when(hallTypeQueryPort.findHallTypeById(5L)).thenReturn(Optional.of(existingHallType));
    when(hallTypeQueryPort.existsHallTypeByNameAndIdNot("diamond", 5L)).thenReturn(true);

    UpdateHallTypeService updateHallTypeService =
        new UpdateHallTypeService(hallTypeQueryPort, hallTypeCommandPort);

    assertThatThrownBy(
            () ->
                updateHallTypeService.updateHallType(
                    5L, new UpdateHallTypeCommand(" diamond ", new BigDecimal("6800000.00"), null)))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Hall type name already exists: diamond");

    verify(hallTypeCommandPort, never())
        .saveHallType(org.mockito.ArgumentMatchers.any(HallType.class));
  }
}
