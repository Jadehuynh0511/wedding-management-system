package com.uit.weddingmanagement.modules.audit.application.usecase;

import com.uit.weddingmanagement.modules.audit.application.port.in.RecordAuditLogUseCase;
import com.uit.weddingmanagement.modules.audit.application.port.out.AuditLogCommandPort;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditActorSnapshot;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditLog;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditResultStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecordAuditLogServiceTest {

    @Mock
    private AuditLogCommandPort auditLogCommandPort;

    @Test
    void shouldSanitizeSensitiveDetailsBeforeInsert() {
        RecordAuditLogService service = new RecordAuditLogService(auditLogCommandPort);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("groupId", 5L);
        details.put("password", "super-secret");
        details.put("nested", Map.of("module", "AUTH", "token", "jwt-secret"));

        service.record(new RecordAuditLogUseCase.RecordAuditLogCommand(
                OffsetDateTime.parse("2026-05-23T12:00:00+07:00"),
                new AuditActorSnapshot(1L, "admin", "ADMIN"),
                "PERMISSION_ASSIGN",
                "AUTH",
                "GROUP_PERMISSION",
                "5",
                "AUDIT_LOG_VIEW",
                AuditResultStatus.SUCCESS,
                "Assigned permission",
                null,
                details));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogCommandPort).insert(captor.capture());

        AuditLog recorded = captor.getValue();
        assertThat(recorded.details()).containsEntry("groupId", 5L);
        assertThat(recorded.details()).doesNotContainKey("password");
        assertThat(recorded.details()).containsKey("nested");
        @SuppressWarnings("unchecked")
        Map<String, Object> nestedDetails = (Map<String, Object>) recorded.details().get("nested");
        assertThat(nestedDetails).containsEntry("module", "AUTH");
        assertThat(nestedDetails).doesNotContainKey("token");
    }
}
