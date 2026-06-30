package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.HallResult;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateHallCommand;

public interface UpdateHallUseCase {

  HallResult updateHall(Long hallId, UpdateHallCommand command);
}
