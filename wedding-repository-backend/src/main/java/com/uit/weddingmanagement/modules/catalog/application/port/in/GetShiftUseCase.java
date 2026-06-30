package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ShiftResult;

public interface GetShiftUseCase {

  ShiftResult getShift(Long shiftId);
}
