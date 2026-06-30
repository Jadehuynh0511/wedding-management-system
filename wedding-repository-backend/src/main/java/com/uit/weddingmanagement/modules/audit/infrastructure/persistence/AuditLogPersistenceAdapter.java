package com.uit.weddingmanagement.modules.audit.infrastructure.persistence;

import com.uit.weddingmanagement.modules.audit.application.port.out.AuditLogCommandPort;
import com.uit.weddingmanagement.modules.audit.application.port.out.AuditLogQueryPort;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditLog;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditResultStatus;
import com.uit.weddingmanagement.modules.audit.infrastructure.persistence.entity.AuditLogJpaEntity;
import com.uit.weddingmanagement.modules.audit.infrastructure.persistence.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

/**
 * Adapter triển khai cổng truy vấn dữ liệu nhật ký hệ thống.
 * Lớp này kết nối giữa domain layer và persistence layer, thực hiện chuyển đổi
 * dữ liệu.
 */
@Component
public class AuditLogPersistenceAdapter implements AuditLogQueryPort, AuditLogCommandPort {

    private final AuditLogRepository auditLogRepository;

    public AuditLogPersistenceAdapter(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Thực hiện tìm kiếm nhật ký và chuyển đổi kết quả từ JPA Entity sang Domain
     * Model.
     */
    @Override
    public Page<AuditLog> search(
            OffsetDateTime from,
            OffsetDateTime to,
            String username,
            String actionCode,
            AuditResultStatus resultStatus,
            Pageable pageable) {
        // Dung Specification de chi them dieu kien khi filter co gia tri,
        // tranh generate SQL "(? is null or ...)" gay loi 42P18 tren PostgreSQL.
        Specification<AuditLogJpaEntity> specification = Specification.where(occurredAtFrom(from))
                .and(occurredAtTo(to))
                .and(actorUsernameContains(username))
                .and(actionCodeEquals(actionCode))
                .and(resultStatusEquals(resultStatus));

        return auditLogRepository.findAll(specification, pageable)
                .map(this::toDomain);
    }

    // Chèn một bản ghi nhật ký mới vào DB, chuyển đổi từ Domain Model sang JPA
    // Entity.
    @Override
    public void insert(AuditLog auditLog) {
        auditLogRepository.save(toEntity(auditLog));
    }

    /**
     * Chuyển đổi từ AuditLogJpaEntity sang AuditLog (Domain Model).
     */
    private AuditLog toDomain(AuditLogJpaEntity entity) {
        return new AuditLog(
                entity.getId(),
                entity.getOccurredAt(),
                entity.getActorUserId(),
                entity.getActorUsername(),
                entity.getActorGroupName(),
                entity.getActionCode(),
                entity.getModuleKey(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getTargetLabel(),
                entity.getResultStatus(),
                entity.getDescription(),
                entity.getErrorMessage(),
                entity.getDetails());
    }

    // Chuyển đổi từ AuditLog (Domain Model) sang AuditLogJpaEntity để lưu vào
    // database.
    private AuditLogJpaEntity toEntity(AuditLog auditLog) {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setOccurredAt(auditLog.occurredAt());
        entity.setActorUserId(auditLog.actorUserId());
        entity.setActorUsername(auditLog.actorUsername());
        entity.setActorGroupName(auditLog.actorGroupName());
        entity.setActionCode(auditLog.actionCode());
        entity.setModuleKey(auditLog.moduleKey());
        entity.setTargetType(auditLog.targetType());
        entity.setTargetId(auditLog.targetId());
        entity.setTargetLabel(auditLog.targetLabel());
        entity.setResultStatus(auditLog.resultStatus());
        entity.setDescription(auditLog.description());
        entity.setErrorMessage(auditLog.errorMessage());
        entity.setDetails(auditLog.details());
        return entity;
    }

    private Specification<AuditLogJpaEntity> occurredAtFrom(OffsetDateTime from) {
        if (from == null) {
            return null;
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("occurredAt"), from);
    }

    private Specification<AuditLogJpaEntity> occurredAtTo(OffsetDateTime to) {
        if (to == null) {
            return null;
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("occurredAt"), to);
    }

    private Specification<AuditLogJpaEntity> actorUsernameContains(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }

        String normalizedUsername = "%" + username.trim() + "%";
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("actorUsername"), normalizedUsername);
    }

    private Specification<AuditLogJpaEntity> actionCodeEquals(String actionCode) {
        if (!StringUtils.hasText(actionCode)) {
            return null;
        }

        String normalizedActionCode = actionCode.trim();
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("actionCode"), normalizedActionCode);
    }

    private Specification<AuditLogJpaEntity> resultStatusEquals(AuditResultStatus resultStatus) {
        if (resultStatus == null) {
            return null;
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("resultStatus"), resultStatus);
    }
}
