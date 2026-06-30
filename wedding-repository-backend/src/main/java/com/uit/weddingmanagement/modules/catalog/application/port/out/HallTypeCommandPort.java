package com.uit.weddingmanagement.modules.catalog.application.port.out;

import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;

public interface HallTypeCommandPort {

  HallType saveHallType(HallType hallType);

  void deleteHallTypeById(Long hallTypeId);
}
