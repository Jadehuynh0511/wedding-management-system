package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.ResourceInUseException;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteHallTypeServiceTest {

  @Mock private HallTypeQueryPort hallTypeQueryPort;

  @Mock private HallTypeCommandPort hallTypeCommandPort;

  @Mock private HallReferenceQueryPort hallReferenceQueryPort;

  @Test
  void shouldDeleteHallTypeWhenNoHallReferencesIt() {
    when(hallTypeQueryPort.findHallTypeById(8L))
        .thenReturn(Optional.of(new HallType(8L, "Emerald", new BigDecimal("5500000.00"), null)));
    when(hallReferenceQueryPort.existsHallByHallTypeId(8L)).thenReturn(false);

    DeleteHallTypeService deleteHallTypeService =
        new DeleteHallTypeService(hallTypeQueryPort, hallTypeCommandPort, hallReferenceQueryPort);

    deleteHallTypeService.deleteHallType(8L);

    verify(hallTypeCommandPort).deleteHallTypeById(8L);
  }

  @Test
  void shouldRejectDeleteWhenHallTypeIsInUse() {
    when(hallTypeQueryPort.findHallTypeById(8L))
        .thenReturn(Optional.of(new HallType(8L, "Emerald", new BigDecimal("5500000.00"), null)));
    when(hallReferenceQueryPort.existsHallByHallTypeId(8L)).thenReturn(true);

    DeleteHallTypeService deleteHallTypeService =
        new DeleteHallTypeService(hallTypeQueryPort, hallTypeCommandPort, hallReferenceQueryPort);

    assertThatThrownBy(() -> deleteHallTypeService.deleteHallType(8L))
        .isInstanceOf(ResourceInUseException.class)
        .hasMessage("Hall type is currently used by existing halls and cannot be deleted.");

    verify(hallTypeCommandPort, never()).deleteHallTypeById(8L);
  }
}
