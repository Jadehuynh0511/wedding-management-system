package com.uit.weddingmanagement.modules.audit.application.usecase;

import com.uit.weddingmanagement.modules.audit.application.port.in.SearchAuditLogsUseCase;
import com.uit.weddingmanagement.modules.audit.application.port.out.AuditLogQueryPort;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service triển khai nghiệp vụ tìm kiếm nhật ký hệ thống.
 * Lớp này thực hiện Input Port (SearchAuditLogsUseCase) và sử dụng Output Port (AuditLogQueryPort).
 */
@Service
@Transactional(readOnly = true)
public class SearchAuditLogsService implements SearchAuditLogsUseCase {

    private final AuditLogQueryPort auditLogQueryPort;

    public SearchAuditLogsService(AuditLogQueryPort auditLogQueryPort) {
        this.auditLogQueryPort = auditLogQueryPort;
    }

    /**
     * Thực hiện logic tìm kiếm bằng cách chuyển tiếp yêu cầu đến cổng truy vấn dữ liệu (Query Port).
     */
    @Override
    public Page<AuditLog> execute(SearchAuditLogsQuery query) {
        return auditLogQueryPort.search(
            query.from(),
            query.to(),
            query.username(),
            query.actionCode(),
            query.resultStatus(),
            query.pageable()
        );
    }
}
