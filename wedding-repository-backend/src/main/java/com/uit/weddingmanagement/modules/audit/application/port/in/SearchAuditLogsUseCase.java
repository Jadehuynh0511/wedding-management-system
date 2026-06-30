package com.uit.weddingmanagement.modules.audit.application.port.in;

import com.uit.weddingmanagement.modules.audit.domain.model.AuditLog;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditResultStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * Interface định nghĩa use case tìm kiếm nhật ký hệ thống.
 */
public interface SearchAuditLogsUseCase {
    /**
     * Thực thi việc tìm kiếm nhật ký hệ thống dựa trên các tiêu chí lọc.
     * @param query Chứa các tiêu chí tìm kiếm và thông tin phân trang.
     * @return Một trang danh sách các bản ghi AuditLog.
     */
    Page<AuditLog> execute(SearchAuditLogsQuery query);

    /**
     * DTO đại diện cho yêu cầu tìm kiếm nhật ký hệ thống.
     */
    record SearchAuditLogsQuery(
        OffsetDateTime from,         // Thời điểm bắt đầu lọc
        OffsetDateTime to,           // Thời điểm kết thúc lọc
        String username,             // Tên đăng nhập người thực hiện
        String actionCode,           // Mã hành động
        AuditResultStatus resultStatus, // Trạng thái kết quả
        Pageable pageable            // Thông tin phân trang (page, size, sort)
    ) {}
}
