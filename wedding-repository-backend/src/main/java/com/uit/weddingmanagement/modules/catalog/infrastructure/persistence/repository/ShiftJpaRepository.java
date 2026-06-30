package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.ShiftJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftJpaRepository extends JpaRepository<ShiftJpaEntity, Long> {

  List<ShiftJpaEntity> findAllByOrderByIdAsc();

  boolean existsByShiftNameIgnoreCase(String shiftName);

  boolean existsByShiftNameIgnoreCaseAndIdNot(String shiftName, Long shiftId);
}
