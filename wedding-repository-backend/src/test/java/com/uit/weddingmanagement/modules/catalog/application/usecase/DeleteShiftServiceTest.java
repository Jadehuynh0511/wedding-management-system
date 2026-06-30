package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.ResourceInUseException;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftBookingReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteShiftServiceTest {

  @Mock private ShiftQueryPort shiftQueryPort;

  @Mock private ShiftCommandPort shiftCommandPort;

  @Mock private ShiftBookingReferenceQueryPort shiftBookingReferenceQueryPort;

  @Test
  void shouldDeleteShiftWhenNoWeddingBookingReferencesIt() {
    when(shiftQueryPort.findShiftById(2L))
        .thenReturn(
            Optional.of(
                new Shift(2L, "Ca tối", LocalTime.of(17, 0), LocalTime.of(21, 30), null)));
    when(shiftBookingReferenceQueryPort.existsWeddingBookingByShiftId(2L)).thenReturn(false);

    DeleteShiftService deleteShiftService =
        new DeleteShiftService(
            shiftQueryPort, shiftCommandPort, shiftBookingReferenceQueryPort);

    deleteShiftService.deleteShift(2L);

    verify(shiftCommandPort).deleteShiftById(2L);
  }

  @Test
  void shouldRejectDeleteWhenShiftIsInUse() {
    when(shiftQueryPort.findShiftById(2L))
        .thenReturn(
            Optional.of(
                new Shift(2L, "Ca tối", LocalTime.of(17, 0), LocalTime.of(21, 30), null)));
    when(shiftBookingReferenceQueryPort.existsWeddingBookingByShiftId(2L)).thenReturn(true);

    DeleteShiftService deleteShiftService =
        new DeleteShiftService(
            shiftQueryPort, shiftCommandPort, shiftBookingReferenceQueryPort);

    assertThatThrownBy(() -> deleteShiftService.deleteShift(2L))
        .isInstanceOf(ResourceInUseException.class)
        .hasMessage("Shift is currently used by existing wedding bookings and cannot be deleted.");

    verify(shiftCommandPort, never()).deleteShiftById(2L);
  }
}
