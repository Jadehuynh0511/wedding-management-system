package com.uit.weddingmanagement.modules.catalog.application.model.result;

import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import java.time.LocalTime;

public record ShiftResult(
    Long id, String shiftName, LocalTime startTime, LocalTime endTime, String description) {

  public static ShiftResult from(Shift shift) {
    return new ShiftResult(
        shift.id(), shift.shiftName(), shift.startTime(), shift.endTime(), shift.description());
  }
}
