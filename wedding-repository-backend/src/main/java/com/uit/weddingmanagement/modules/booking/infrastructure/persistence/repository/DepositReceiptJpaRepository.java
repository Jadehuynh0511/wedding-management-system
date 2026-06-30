package com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.DepositReceiptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositReceiptJpaRepository extends JpaRepository<DepositReceiptJpaEntity, Long> {}
