package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.HallTypeResult;

public interface GetHallTypeUseCase {

  HallTypeResult getHallType(Long hallTypeId);
}
