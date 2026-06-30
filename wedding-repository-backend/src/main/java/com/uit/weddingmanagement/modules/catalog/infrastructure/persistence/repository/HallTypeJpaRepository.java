package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.HallTypeJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HallTypeJpaRepository extends JpaRepository<HallTypeJpaEntity, Long> {

  List<HallTypeJpaEntity> findAllByOrderByIdAsc();

  boolean existsByHallTypeNameIgnoreCase(String hallTypeName);

  boolean existsByHallTypeNameIgnoreCaseAndIdNot(String hallTypeName, Long id);
}
