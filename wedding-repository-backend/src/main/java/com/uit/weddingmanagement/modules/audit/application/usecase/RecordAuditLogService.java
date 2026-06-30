package com.uit.weddingmanagement.modules.audit.application.usecase;

import com.uit.weddingmanagement.modules.audit.application.port.in.RecordAuditLogUseCase;
import com.uit.weddingmanagement.modules.audit.application.port.out.AuditLogCommandPort;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditActorSnapshot;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditLog;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditResultStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Use case ghi audit log qua transaction riêng để vẫn giữ lại log FAIL khi
 * transaction chính (của nghiệp vụ) rollback
 */
@Service
public class RecordAuditLogService implements RecordAuditLogUseCase {

    // Danh sách các từ khóa nhạy cảm để lọc bỏ khỏi chi tiết log, nghĩa là không
    // ghi lại giá trị của các field có tên chứa các từ khóa này vào table audit
    // log, nhằm tránh rủi ro lộ thông tin nhạy cảm khi truy vấn hoặc phân tích log.
    private static final List<String> SENSITIVE_KEYWORDS = List.of("password", "token", "secret", "hash");

    private final AuditLogCommandPort auditLogCommandPort;

    public RecordAuditLogService(AuditLogCommandPort auditLogCommandPort) {
        this.auditLogCommandPort = auditLogCommandPort;
    }

    // Hàm này có tác dụng ghi một bản ghi audit log mới vào hệ thống.
    // Nó sẽ nhận một RecordAuditLogCommand chứa
    // tất cả thông tin cần thiết để tạo một bản ghi audit log, sau đó chuẩn hóa và
    // làm sạch dữ liệu trước khi gọi AuditLogCommandPort để lưu trữ bản ghi vào
    // persistence.
    @Override
    // Đây là Propagation trong transaction management của Spring
    // Trường hợp này tạo transaction mới cho việc ghi log, bất kể transaction của nghiệp vụ chính thế nào,
    // để đảm bảo log vẫn được ghi lại ngay cả khi nghiệp vụ chính rollback.
    @Transactional(propagation = Propagation.REQUIRES_NEW) 
    public void record(RecordAuditLogCommand command) {
        AuditActorSnapshot actor = command.actor() == null ? AuditActorSnapshot.system() : command.actor();

        AuditLog auditLog = new AuditLog(
                null,
                command.occurredAt() == null ? OffsetDateTime.now() : command.occurredAt(),
                actor.userId(),
                normalizeRequired(actor.username(), "SYSTEM"),
                normalizeRequired(actor.groupName(), "SYSTEM"),
                normalizeRequired(command.actionCode(), "UNKNOWN_ACTION"),
                normalizeRequired(command.moduleKey(), "UNKNOWN_MODULE"),
                normalizeRequired(command.targetType(), "UNKNOWN_TARGET"),
                normalizeOptional(command.targetId()),
                normalizeOptional(command.targetLabel()),
                command.resultStatus() == null ? AuditResultStatus.FAIL : command.resultStatus(),
                normalizeRequired(command.description(), "Audit action executed."),
                normalizeOptional(command.errorMessage()),
                sanitizeDetails(command.details()));

        auditLogCommandPort.insert(auditLog);
    }

    private String normalizeRequired(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Map<String, Object> sanitizeDetails(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return null;
        }

        Map<String, Object> sanitized = sanitizeMap(details);
        return sanitized.isEmpty() ? null : sanitized;
    }

    private Map<String, Object> sanitizeMap(Map<String, Object> source) {
        Map<String, Object> sanitized = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.hasText(key) || isSensitiveKey(key)) {
                continue;
            }

            Object sanitizedValue = sanitizeValue(entry.getValue());
            if (sanitizedValue != null) {
                sanitized.put(key, sanitizedValue);
            }
        }

        return sanitized;
    }

    private Object sanitizeValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> nested = new LinkedHashMap<>();

            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                Object rawKey = entry.getKey();
                if (!(rawKey instanceof String stringKey)
                        || !StringUtils.hasText(stringKey)
                        || isSensitiveKey(stringKey)) {
                    continue;
                }

                Object sanitizedNestedValue = sanitizeValue(entry.getValue());
                if (sanitizedNestedValue != null) {
                    nested.put(stringKey, sanitizedNestedValue);
                }
            }

            return nested.isEmpty() ? null : nested;
        }

        if (value instanceof Iterable<?> iterable) {
            List<Object> sanitizedList = new ArrayList<>();

            for (Object item : iterable) {
                Object sanitizedItem = sanitizeValue(item);
                if (sanitizedItem != null) {
                    sanitizedList.add(sanitizedItem);
                }
            }

            return sanitizedList.isEmpty() ? null : sanitizedList;
        }

        if (value.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            List<Object> sanitizedList = new ArrayList<>(length);

            for (int index = 0; index < length; index++) {
                Object sanitizedItem = sanitizeValue(java.lang.reflect.Array.get(value, index));
                if (sanitizedItem != null) {
                    sanitizedList.add(sanitizedItem);
                }
            }

            return sanitizedList.isEmpty() ? null : sanitizedList;
        }

        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return value;
        }

        return value.toString();
    }

    private boolean isSensitiveKey(String key) {
        String normalizedKey = key.toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYWORDS.stream().anyMatch(normalizedKey::contains);
    }
}
