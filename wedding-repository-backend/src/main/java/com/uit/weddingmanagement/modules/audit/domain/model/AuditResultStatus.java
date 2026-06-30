package com.uit.weddingmanagement.modules.audit.domain.model;

/**
 * Định nghĩa các trạng thái kết quả của một hành động được ghi lại trong Audit Log.
 */
public enum AuditResultStatus {
    SUCCESS, // Hành động thực hiện thành công
    FAIL     // Hành động thực hiện thất bại
}
