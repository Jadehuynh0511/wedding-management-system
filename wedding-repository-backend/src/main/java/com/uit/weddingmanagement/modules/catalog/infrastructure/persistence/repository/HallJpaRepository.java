package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.HallJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// Tầng repository là nơi định nghĩa các phương thức truy vấn dữ liệu từ DB
public interface HallJpaRepository extends JpaRepository<HallJpaEntity, Long> {

  List<HallJpaEntity> findAllByOrderByIdAsc();

  boolean existsByHallType_Id(Long hallTypeId);

  boolean existsByHallNameIgnoreCase(String hallName);

  boolean existsByHallNameIgnoreCaseAndIdNot(String hallName, Long hallId);
}
