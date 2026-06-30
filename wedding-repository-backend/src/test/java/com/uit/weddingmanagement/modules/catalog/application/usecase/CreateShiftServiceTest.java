package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateShiftCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateShiftServiceTest {

  @Mock private ShiftQueryPort shiftQueryPort;

  @Mock private ShiftCommandPort shiftCommandPort;

  @Captor private ArgumentCaptor<Shift> shiftCaptor;

  @Test
  void shouldCreateShiftWhenRequestIsValid() {
    when(shiftQueryPort.existsShiftByName("Ca chiều")).thenReturn(false);
    when(shiftQueryPort.findAllShifts())
        .thenReturn(
            List.of(new Shift(1L, "Ca trưa", LocalTime.of(11, 0), LocalTime.of(15, 0), null)));
    when(shiftCommandPort.saveShift(any(Shift.class)))
        .thenReturn(
            new Shift(
                3L,
                "Ca chiều",
                LocalTime.of(15, 30),
                LocalTime.of(19, 30),
                "Khung giờ chuyển tiếp"));

    CreateShiftService createShiftService =
        new CreateShiftService(shiftQueryPort, shiftCommandPort);

    var result =
        createShiftService.createShift(
            new CreateShiftCommand(
                "  Ca   chiều  ",
                LocalTime.of(15, 30),
                LocalTime.of(19, 30),
                "  Khung giờ chuyển tiếp  "));

    assertThat(result.id()).isEqualTo(3L);
    assertThat(result.shiftName()).isEqualTo("Ca chiều");
    assertThat(result.startTime()).isEqualTo(LocalTime.of(15, 30));
    assertThat(result.endTime()).isEqualTo(LocalTime.of(19, 30));
    assertThat(result.description()).isEqualTo("Khung giờ chuyển tiếp");

    verify(shiftCommandPort).saveShift(shiftCaptor.capture());
    Shift savedShift = shiftCaptor.getValue();
    assertThat(savedShift.id()).isNull();
    assertThat(savedShift.shiftName()).isEqualTo("Ca chiều");
    assertThat(savedShift.startTime()).isEqualTo(LocalTime.of(15, 30));
    assertThat(savedShift.endTime()).isEqualTo(LocalTime.of(19, 30));
    assertThat(savedShift.description()).isEqualTo("Khung giờ chuyển tiếp");
  }

  @Test
  void shouldRejectDuplicateShiftNameIgnoringCase() {
    when(shiftQueryPort.existsShiftByName("ca tối")).thenReturn(true);

    CreateShiftService createShiftService =
        new CreateShiftService(shiftQueryPort, shiftCommandPort);

    assertThatThrownBy(
            () ->
                createShiftService.createShift(
                    new CreateShiftCommand(
                        " ca tối ",
                        LocalTime.of(17, 0),
                        LocalTime.of(21, 30),
                        null)))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Shift name already exists: ca tối");

    verify(shiftCommandPort, never()).saveShift(any(Shift.class));
  }

  @Test
  void shouldRejectWhenShiftTimeRangeOverlapsExistingShift() {
    when(shiftQueryPort.existsShiftByName("Ca chiều")).thenReturn(false);
    when(shiftQueryPort.findAllShifts())
        .thenReturn(
            List.of(new Shift(1L, "Ca trưa", LocalTime.of(11, 0), LocalTime.of(15, 0), null)));

    CreateShiftService createShiftService =
        new CreateShiftService(shiftQueryPort, shiftCommandPort);

    assertThatThrownBy(
            () ->
                createShiftService.createShift(
                    new CreateShiftCommand(
                        "Ca chiều",
                        LocalTime.of(15, 0),
                        LocalTime.of(18, 0),
                        "Bị chồng giờ")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Shift time range overlaps with existing shift: Ca trưa");

    verify(shiftCommandPort, never()).saveShift(any(Shift.class));
  }
}
