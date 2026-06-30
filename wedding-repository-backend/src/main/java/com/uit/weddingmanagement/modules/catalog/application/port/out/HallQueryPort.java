package com.uit.weddingmanagement.modules.catalog.application.port.out;

import com.uit.weddingmanagement.modules.catalog.domain.model.Hall;
import java.util.List;
import java.util.Optional;

public interface HallQueryPort {

  List<Hall> findAllHalls();

  Optional<Hall> findHallById(Long hallId);

  boolean existsHallByName(String hallName);

  boolean existsHallByNameAndIdNot(String hallName, Long hallId);
}
