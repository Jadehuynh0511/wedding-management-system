package com.uit.weddingmanagement.modules.audit.application.port.out;

import com.uit.weddingmanagement.modules.audit.domain.model.AuditLog;

/**
 * Output port chỉ phụ trách insert audit log mới vào persistence
 */
public interface AuditLogCommandPort {

    void insert(AuditLog auditLog);
}
