package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateShiftCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateShiftServiceTest {

  @Mock private ShiftQueryPort shiftQueryPort;

  @Mock private ShiftCommandPort shiftCommandPort;

  @Captor private ArgumentCaptor<Shift> shiftCaptor;

  @Test
  void shouldUpdateShiftWhenRequestIsValid() {
    Shift currentShift =
        new Shift(2L, "Ca tối", LocalTime.of(17, 0), LocalTime.of(21, 30), "Khung giờ buổi tối");

    when(shiftQueryPort.findShiftById(2L)).thenReturn(Optional.of(currentShift));
    when(shiftQueryPort.existsShiftByNameAndIdNot("Ca chiều", 2L)).thenReturn(false);
    when(shiftQueryPort.findAllShifts())
        .thenReturn(
            List.of(
                new Shift(1L, "Ca trưa", LocalTime.of(11, 0), LocalTime.of(15, 0), null),
                currentShift));
    when(shiftCommandPort.saveShift(any(Shift.class)))
        .thenReturn(
            new Shift(
                2L,
                "Ca chiều",
                LocalTime.of(15, 30),
                LocalTime.of(19, 30),
                "Khung giờ chuyển tiếp"));

    UpdateShiftService updateShiftService =
        new UpdateShiftService(shiftQueryPort, shiftCommandPort);

    var result =
        updateShiftService.updateShift(
            2L,
            new UpdateShiftCommand(
                "  Ca   chiều  ",
                LocalTime.of(15, 30),
                LocalTime.of(19, 30),
                "  Khung giờ chuyển tiếp  "));

    assertThat(result.id()).isEqualTo(2L);
    assertThat(result.shiftName()).isEqualTo("Ca chiều");
    assertThat(result.startTime()).isEqualTo(LocalTime.of(15, 30));
    assertThat(result.endTime()).isEqualTo(LocalTime.of(19, 30));

    verify(shiftCommandPort).saveShift(shiftCaptor.capture());
    Shift savedShift = shiftCaptor.getValue();
    assertThat(savedShift.id()).isEqualTo(2L);
    assertThat(savedShift.shiftName()).isEqualTo("Ca chiều");
    assertThat(savedShift.startTime()).isEqualTo(LocalTime.of(15, 30));
    assertThat(savedShift.endTime()).isEqualTo(LocalTime.of(19, 30));
    assertThat(savedShift.description()).isEqualTo("Khung giờ chuyển tiếp");
  }

  @Test
  void shouldRejectDuplicateShiftNameIgnoringCaseWhenUpdating() {
    Shift currentShift =
        new Shift(2L, "Ca tối", LocalTime.of(17, 0), LocalTime.of(21, 30), "Khung giờ buổi tối");

    when(shiftQueryPort.findShiftById(2L)).thenReturn(Optional.of(currentShift));
    when(shiftQueryPort.existsShiftByNameAndIdNot("ca trưa", 2L)).thenReturn(true);

    UpdateShiftService updateShiftService =
        new UpdateShiftService(shiftQueryPort, shiftCommandPort);

    assertThatThrownBy(
            () ->
                updateShiftService.updateShift(
                    2L,
                    new UpdateShiftCommand(
                        " ca trưa ",
                        LocalTime.of(17, 0),
                        LocalTime.of(21, 30),
                        null)))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Shift name already exists: ca trưa");

    verify(shiftCommandPort, never()).saveShift(any(Shift.class));
  }

  @Test
  void shouldRejectWhenUpdatedTimeRangeOverlapsAnotherShift() {
    Shift currentShift =
        new Shift(2L, "Ca tối", LocalTime.of(17, 0), LocalTime.of(21, 30), "Khung giờ buổi tối");

    when(shiftQueryPort.findShiftById(2L)).thenReturn(Optional.of(currentShift));
    when(shiftQueryPort.existsShiftByNameAndIdNot("Ca chiều", 2L)).thenReturn(false);
    when(shiftQueryPort.findAllShifts())
        .thenReturn(
            List.of(
                new Shift(1L, "Ca trưa", LocalTime.of(11, 0), LocalTime.of(15, 0), null),
                currentShift));

    UpdateShiftService updateShiftService =
        new UpdateShiftService(shiftQueryPort, shiftCommandPort);

    assertThatThrownBy(
            () ->
                updateShiftService.updateShift(
                    2L,
                    new UpdateShiftCommand(
                        "Ca chiều",
                        LocalTime.of(15, 0),
                        LocalTime.of(18, 0),
                        "Bị chồng giờ")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Shift time range overlaps with existing shift: Ca trưa");

    verify(shiftCommandPort, never()).saveShift(any(Shift.class));
  }
}
