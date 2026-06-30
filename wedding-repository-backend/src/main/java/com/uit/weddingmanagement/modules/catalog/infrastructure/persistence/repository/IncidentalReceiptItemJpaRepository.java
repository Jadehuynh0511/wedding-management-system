package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.IncidentalReceiptItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentalReceiptItemJpaRepository
    extends JpaRepository<IncidentalReceiptItemJpaEntity, Long> {

  boolean existsByServiceItem_Id(Long serviceItemId);
}
