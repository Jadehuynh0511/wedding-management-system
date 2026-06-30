package com.uit.weddingmanagement.modules.catalog.application.port.out;

import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;

public interface ShiftCommandPort {

  Shift saveShift(Shift shift);

  void deleteShiftById(Long shiftId);
}
