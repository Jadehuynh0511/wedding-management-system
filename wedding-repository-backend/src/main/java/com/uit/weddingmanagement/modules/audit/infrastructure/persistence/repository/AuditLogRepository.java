package com.uit.weddingmanagement.modules.audit.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.audit.infrastructure.persistence.entity.AuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository cho phep query audit log qua JPA va Specification de tranh loi SQL khi filter null.
 */
@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLogJpaEntity, Long>, JpaSpecificationExecutor<AuditLogJpaEntity> {}
