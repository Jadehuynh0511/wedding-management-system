package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateHallTypeCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.result.HallTypeResult;

public interface CreateHallTypeUseCase {

  HallTypeResult createHallType(CreateHallTypeCommand command);
}
