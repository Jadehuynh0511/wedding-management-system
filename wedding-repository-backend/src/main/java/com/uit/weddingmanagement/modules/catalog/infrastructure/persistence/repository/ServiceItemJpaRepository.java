package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.ServiceItemJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceItemJpaRepository extends JpaRepository<ServiceItemJpaEntity, Long> {

  boolean existsByServiceNameIgnoreCase(String serviceName);

  boolean existsByServiceNameIgnoreCaseAndIdNot(String serviceName, Long serviceItemId);

  @Query(
      """
      select serviceItem
      from ServiceItemJpaEntity serviceItem
      where (:status is null or serviceItem.status = :status)
      order by serviceItem.serviceName asc
      """)
  List<ServiceItemJpaEntity> findAllByOptionalStatus(@Param("status") ServiceItemStatus status);
}
