package com.uit.weddingmanagement.modules.audit.application.port.out;

import com.uit.weddingmanagement.modules.audit.domain.model.AuditLog;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditResultStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * Interface cung cấp các phương thức truy vấn nhật ký hệ thống từ cơ sở dữ liệu.
 */
public interface AuditLogQueryPort {
    /**
     * Tìm kiếm các bản ghi nhật ký hệ thống.
     *
     * @param from Thời điểm bắt đầu
     * @param to Thời điểm kết thúc
     * @param username Tên đăng nhập
     * @param actionCode Mã hành động
     * @param resultStatus Trạng thái kết quả
     * @param pageable Thông tin phân trang
     * @return Danh sách các bản ghi AuditLog theo trang
     */
    Page<AuditLog> search(
        OffsetDateTime from,
        OffsetDateTime to,
        String username,
        String actionCode,
        AuditResultStatus resultStatus,
        Pageable pageable
    );
}
