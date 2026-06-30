package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.HallTypeResult;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateHallTypeCommand;

public interface UpdateHallTypeUseCase {

  HallTypeResult updateHallType(Long hallTypeId, UpdateHallTypeCommand command);
}
