package com.uit.weddingmanagement.modules.audit.presentation.controller;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.modules.audit.application.port.in.SearchAuditLogsUseCase;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditResultStatus;
import com.uit.weddingmanagement.modules.audit.presentation.dto.response.AuditLogPageResponse;
import com.uit.weddingmanagement.modules.audit.presentation.dto.response.AuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/**
 * Chỉ cho phép truy cập đọc và yêu cầu quyền AUDIT_LOG_VIEW.
 */
@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Logs", description = "Quyền truy cập chỉ đọc vào nhật ký hoạt động của hệ thống.")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final SearchAuditLogsUseCase searchAuditLogsUseCase;

    public AuditLogController(SearchAuditLogsUseCase searchAuditLogsUseCase) {
        this.searchAuditLogsUseCase = searchAuditLogsUseCase;
    }

    /**
     * API tìm kiếm nhật ký hệ thống có phân trang và lọc.
     *
     * @param from Thời điểm bắt đầu lọc (ISO Date Time)
     * @param to Thời điểm kết thúc lọc (ISO Date Time)
     * @param username Lọc theo tên người thực hiện
     * @param actionCode Lọc theo mã hành động
     * @param resultStatus Lọc theo trạng thái kết quả (SUCCESS/FAIL)
     * @param page Số trang (mặc định 0)
     * @param size Số lượng bản ghi mỗi trang (mặc định 20)
     * @return Phản hồi API chứa danh sách nhật ký đã được phân trang
     */
    @GetMapping
    @Operation(summary = "Tìm kiếm nhật ký hệ thống", description = "Lấy danh sách nhật ký hoạt động có phân trang và lọc. Yêu cầu quyền AUDIT_LOG_VIEW.")
    @PreAuthorize("@authorizationService.hasPermission('AUDIT_LOG_VIEW')")
    public ApiResponse<AuditLogPageResponse> searchAuditLogs(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String actionCode,
        @RequestParam(required = false) AuditResultStatus resultStatus,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        // Chuyển đổi các tham số yêu cầu thành Query object cho Use Case
        Page<AuditLogResponse> resultPage = searchAuditLogsUseCase.execute(
            new SearchAuditLogsUseCase.SearchAuditLogsQuery(
                from,
                to,
                username,
                actionCode,
                resultStatus,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"))
            )
        ).map(AuditLogResponse::fromDomain);

        // Đóng gói kết quả vào DTO phản hồi phân trang
        AuditLogPageResponse response = new AuditLogPageResponse(
            resultPage.getContent(),
            resultPage.getTotalElements(),
            resultPage.getTotalPages(),
            resultPage.getNumber(),
            resultPage.getSize()
        );

        return ApiResponse.success("Lấy danh sách nhật ký hệ thống thành công.", response);
    }
}
