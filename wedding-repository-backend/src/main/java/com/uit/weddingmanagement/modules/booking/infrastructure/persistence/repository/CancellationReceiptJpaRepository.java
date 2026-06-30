package com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.CancellationReceiptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancellationReceiptJpaRepository
    extends JpaRepository<CancellationReceiptJpaEntity, Long> {}
