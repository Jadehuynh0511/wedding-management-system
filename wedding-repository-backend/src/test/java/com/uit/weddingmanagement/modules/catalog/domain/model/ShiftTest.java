package com.uit.weddingmanagement.modules.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ShiftTest {

  @Test
  void shouldNormalizeFieldsWhenCreatingShift() {
    Shift shift =
        Shift.create(
            "  Ca   chiều  ",
            LocalTime.of(15, 30),
            LocalTime.of(19, 30),
            "  Khung giờ chuyển tiếp  ");

    assertThat(shift.id()).isNull();
    assertThat(shift.shiftName()).isEqualTo("Ca chiều");
    assertThat(shift.startTime()).isEqualTo(LocalTime.of(15, 30));
    assertThat(shift.endTime()).isEqualTo(LocalTime.of(19, 30));
    assertThat(shift.description()).isEqualTo("Khung giờ chuyển tiếp");
  }

  @Test
  void shouldRejectWhenEndTimeIsNotAfterStartTime() {
    assertThatThrownBy(
            () ->
                Shift.create(
                    "Ca trưa", LocalTime.of(11, 0), LocalTime.of(11, 0), "Không hợp lệ"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Shift end time must be after start time.");
  }

  @Test
  void shouldTreatTouchingBoundariesAsOverlapFollowingClosedIntervalRule() {
    Shift lunchShift =
        new Shift(
            1L, "Ca trưa", LocalTime.of(11, 0), LocalTime.of(15, 0), "Khung giờ buổi trưa");

    assertThat(lunchShift.overlapsWith(LocalTime.of(15, 0), LocalTime.of(18, 0))).isTrue();
  }
}
