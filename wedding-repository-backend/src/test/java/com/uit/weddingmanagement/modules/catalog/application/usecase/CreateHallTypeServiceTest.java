package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateHallTypeCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateHallTypeServiceTest {

  @Mock private HallTypeQueryPort hallTypeQueryPort;

  @Mock private HallTypeCommandPort hallTypeCommandPort;

  @Captor private ArgumentCaptor<HallType> hallTypeCaptor;

  @Test
  void shouldCreateHallTypeWhenNameIsUnique() {
    when(hallTypeQueryPort.existsHallTypeByName("Ruby Elite")).thenReturn(false);
    when(hallTypeCommandPort.saveHallType(org.mockito.ArgumentMatchers.any(HallType.class)))
        .thenReturn(
            new HallType(10L, "Ruby Elite", new BigDecimal("4500000.00"), "Premium concept"));

    CreateHallTypeService createHallTypeService =
        new CreateHallTypeService(hallTypeQueryPort, hallTypeCommandPort);

    var result =
        createHallTypeService.createHallType(
            new CreateHallTypeCommand(
                "  Ruby   Elite  ", new BigDecimal("4500000.00"), "  Premium concept  "));

    assertThat(result.id()).isEqualTo(10L);
    assertThat(result.hallTypeName()).isEqualTo("Ruby Elite");
    assertThat(result.minimumTablePrice()).isEqualByComparingTo("4500000.00");
    assertThat(result.description()).isEqualTo("Premium concept");

    verify(hallTypeCommandPort).saveHallType(hallTypeCaptor.capture());
    HallType savedHallType = hallTypeCaptor.getValue();
    assertThat(savedHallType.id()).isNull();
    assertThat(savedHallType.hallTypeName()).isEqualTo("Ruby Elite");
    assertThat(savedHallType.minimumTablePrice()).isEqualByComparingTo("4500000.00");
    assertThat(savedHallType.description()).isEqualTo("Premium concept");
  }

  @Test
  void shouldRejectDuplicateHallTypeNameIgnoringCase() {
    when(hallTypeQueryPort.existsHallTypeByName("ruby")).thenReturn(true);

    CreateHallTypeService createHallTypeService =
        new CreateHallTypeService(hallTypeQueryPort, hallTypeCommandPort);

    assertThatThrownBy(
            () ->
                createHallTypeService.createHallType(
                    new CreateHallTypeCommand(" ruby ", new BigDecimal("3200000.00"), null)))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Hall type name already exists: ruby");

    verify(hallTypeCommandPort, never())
        .saveHallType(org.mockito.ArgumentMatchers.any(HallType.class));
  }
}
