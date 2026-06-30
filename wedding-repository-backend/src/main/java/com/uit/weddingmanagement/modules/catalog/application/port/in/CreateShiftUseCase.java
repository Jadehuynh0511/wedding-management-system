package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateShiftCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.result.ShiftResult;

public interface CreateShiftUseCase {

  ShiftResult createShift(CreateShiftCommand command);
}
