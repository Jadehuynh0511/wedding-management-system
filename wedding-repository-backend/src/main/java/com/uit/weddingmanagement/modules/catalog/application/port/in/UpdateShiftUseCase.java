package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ShiftResult;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateShiftCommand;

public interface UpdateShiftUseCase {

  ShiftResult updateShift(Long shiftId, UpdateShiftCommand command);
}
