package com.uit.weddingmanagement.modules.catalog.application.port.out;

import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import java.util.List;
import java.util.Optional;

public interface HallTypeQueryPort {

  List<HallType> findAllHallTypes();

  Optional<HallType> findHallTypeById(Long hallTypeId);

  boolean existsHallTypeByName(String hallTypeName);

  boolean existsHallTypeByNameAndIdNot(String hallTypeName, Long hallTypeId);
}
