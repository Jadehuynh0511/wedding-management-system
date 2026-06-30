package com.uit.weddingmanagement.modules.audit.application.port.in;

import com.uit.weddingmanagement.modules.audit.domain.model.AuditActorSnapshot;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditResultStatus;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Use case ghi một bản ghi audit log mới theo transaction riêng, đảm bảo rằng
 * việc ghi log không ảnh hưởng đến hiệu suất và tính ổn định của luồng nghiệp
 * vụ chính.
 * Use case này sẽ được gọi từ Aspect sau khi đã thu thập đủ thông tin
 * cần thiết từ annotation @AuditAction và đánh giá các biểu thức SpEL để lấy
 * thông tin chi tiết về hành động, đối tượng bị tác động, kết quả của hành
 * động, và các thông tin bổ sung khác.
 */
public interface RecordAuditLogUseCase {

    void record(RecordAuditLogCommand command);

    // Command object chứa tất cả thông tin cần thiết để ghi một bản ghi audit log
    record RecordAuditLogCommand(
            OffsetDateTime occurredAt,
            AuditActorSnapshot actor,
            String actionCode,
            String moduleKey,
            String targetType,
            String targetId,
            String targetLabel,
            AuditResultStatus resultStatus,
            String description,
            String errorMessage,
            Map<String, Object> details) {
    }
}
