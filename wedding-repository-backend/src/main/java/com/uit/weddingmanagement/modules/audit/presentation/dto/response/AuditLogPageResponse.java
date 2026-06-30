package com.uit.weddingmanagement.modules.audit.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * DTO response chứa danh sách nhật ký hệ thống đã được phân trang.
 */
@Schema(name = "AuditLogPageResponse", description = "Danh sách nhật ký hệ thống có phân trang.")
public record AuditLogPageResponse(
    @Schema(description = "Danh sách các bản ghi nhật ký.")
    List<AuditLogResponse> items,

    @Schema(description = "Tổng số bản ghi tìm thấy.")
    long totalElements,

    @Schema(description = "Tổng số trang.")
    int totalPages,

    @Schema(description = "Số thứ tự trang hiện tại (bắt đầu từ 0).")
    int page,

    @Schema(description = "Số lượng bản ghi trên mỗi trang.")
    int size
) {
}
