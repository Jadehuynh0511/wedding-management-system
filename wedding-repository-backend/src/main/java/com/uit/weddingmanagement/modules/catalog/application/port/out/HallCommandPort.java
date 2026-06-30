package com.uit.weddingmanagement.modules.catalog.application.port.out;

import com.uit.weddingmanagement.modules.catalog.domain.model.Hall;

public interface HallCommandPort {

  Hall saveHall(Hall hall);

  void deleteHallById(Long hallId);
}
