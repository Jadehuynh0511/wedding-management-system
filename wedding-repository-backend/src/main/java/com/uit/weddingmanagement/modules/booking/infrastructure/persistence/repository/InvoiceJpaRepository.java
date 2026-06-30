package com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.InvoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceJpaRepository
    extends JpaRepository<InvoiceJpaEntity, Long>, JpaSpecificationExecutor<InvoiceJpaEntity> {}
