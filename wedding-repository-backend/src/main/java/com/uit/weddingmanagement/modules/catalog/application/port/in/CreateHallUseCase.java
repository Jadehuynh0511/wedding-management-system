package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateHallCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.result.HallResult;

public interface CreateHallUseCase {

  HallResult createHall(CreateHallCommand command);
}
