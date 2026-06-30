package com.uit.weddingmanagement.modules.audit.presentation.dto.response;

import com.uit.weddingmanagement.modules.audit.domain.model.AuditLog;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditResultStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * DTO response chứa thông tin chi tiết của một bản ghi nhật ký hệ thống.
 */
@Schema(name = "AuditLogResponse", description = "Chi tiết một bản ghi nhật ký hệ thống.")
public record AuditLogResponse(
    @Schema(description = "ID định danh trong cơ sở dữ liệu.", example = "100")
    Long id,

    @Schema(description = "Thời điểm xảy ra hành động.")
    OffsetDateTime occurredAt,

    @Schema(description = "ID của người dùng thực hiện hành động.", example = "1")
    Long actorUserId,

    @Schema(description = "Tên đăng nhập của người thực hiện.", example = "admin")
    String actorUsername,

    @Schema(description = "Tên nhóm của người thực hiện.", example = "ADMIN")
    String actorGroupName,

    @Schema(description = "Mã hành động nghiệp vụ đã thực hiện.", example = "PERMISSION_ASSIGN")
    String actionCode,

    @Schema(description = "Module bị tác động.", example = "AUTH")
    String moduleKey,

    @Schema(description = "Loại đối tượng mục tiêu.", example = "USER_GROUP")
    String targetType,

    @Schema(description = "ID hoặc khóa nghiệp vụ của đối tượng mục tiêu.", example = "5")
    String targetId,

    @Schema(description = "Nhãn hiển thị của đối tượng mục tiêu.", example = "Moderators")
    String targetLabel,

    @Schema(description = "Kết quả của hành động.")
    AuditResultStatus resultStatus,

    @Schema(description = "Mô tả dễ hiểu về hành động.", example = "Đã gán 5 quyền cho nhóm Moderators")
    String description,

    @Schema(description = "Thông báo lỗi nếu hành động thất bại.", example = "Không tìm thấy nhóm")
    String errorMessage,

    @Schema(description = "Dữ liệu bổ sung đi kèm.")
    Map<String, Object> details
) {
    /**
     * Chuyển đổi từ Domain Model sang DTO phản hồi.
     */
    public static AuditLogResponse fromDomain(AuditLog domain) {
        return new AuditLogResponse(
            domain.id(),
            domain.occurredAt(),
            domain.actorUserId(),
            domain.actorUsername(),
            domain.actorGroupName(),
            domain.actionCode(),
            domain.moduleKey(),
            domain.targetType(),
            domain.targetId(),
            domain.targetLabel(),
            domain.resultStatus(),
            domain.description(),
            domain.errorMessage(),
            domain.details()
        );
    }
}
