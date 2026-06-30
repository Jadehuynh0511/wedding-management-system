package com.uit.weddingmanagement.modules.audit.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Model đại diện cho một bản ghi nhật ký hệ thống (Audit Log).
 * Record này chứa thông tin chi tiết về các hành động người dùng thực hiện trên hệ thống.
 */
public record AuditLog(
    Long id,               // ID duy nhất của bản ghi
    OffsetDateTime occurredAt, // Thời điểm xảy ra hành động
    Long actorUserId,      // ID của người thực hiện hành động
    String actorUsername,  // Tên đăng nhập của người thực hiện
    String actorGroupName, // Tên nhóm của người thực hiện (nếu có)
    String actionCode,     // Mã hành động (ví dụ: CREATE_USER, UPDATE_WEDDING)
    String moduleKey,      // Khóa của module liên quan (ví dụ: USER, AUDIT)
    String targetType,     // Loại đối tượng bị tác động
    String targetId,       // ID của đối tượng bị tác động
    String targetLabel,    // Nhãn hiển thị của đối tượng bị tác động
    AuditResultStatus resultStatus, // Trạng thái kết quả của hành động (SUCCESS/FAIL)
    String description,    // Mô tả chi tiết về hành động
    String errorMessage,   // Thông báo lỗi (nếu có)
    Map<String, Object> details // Các thông tin chi tiết bổ sung dưới dạng key-value
) {
}
