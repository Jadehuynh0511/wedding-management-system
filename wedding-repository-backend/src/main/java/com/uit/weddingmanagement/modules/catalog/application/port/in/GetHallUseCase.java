package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.HallResult;

public interface GetHallUseCase {

  HallResult getHall(Long hallId);
}
