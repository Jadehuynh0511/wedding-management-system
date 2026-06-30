package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.ServicePriceHistoryJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicePriceHistoryJpaRepository
    extends JpaRepository<ServicePriceHistoryJpaEntity, Long> {

  List<ServicePriceHistoryJpaEntity> findByServiceItem_IdOrderByEffectiveToDescIdDesc(Long serviceItemId);
}
