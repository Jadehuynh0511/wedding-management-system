package com.uit.weddingmanagement.modules.audit.infrastructure.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.weddingmanagement.modules.audit.application.port.in.RecordAuditLogUseCase;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditResultStatus;
import com.uit.weddingmanagement.modules.auth.application.model.command.AssignPermissionCommand;
import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditActionAspectTest {

    @Mock
    private RecordAuditLogUseCase recordAuditLogUseCase;

    @Mock
    private CurrentUserPort currentUserPort;

    private TestAuditService proxy;

    @BeforeEach
    void setUp() {
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser(
                1L,
                "admin",
                "Local Admin",
                1L,
                "ADMIN",
                Set.of("AUDIT_LOG_VIEW")));

        AuditActionAspect auditActionAspect =
                new AuditActionAspect(recordAuditLogUseCase, currentUserPort, new ObjectMapper());
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new TestAuditService());
        proxyFactory.addAspect(auditActionAspect);
        proxy = proxyFactory.getProxy();
    }

    @Test
    void shouldRecordSuccessAuditLogWhenAnnotatedMethodSucceeds() {
        boolean changed = proxy.assignPermission(new AssignPermissionCommand(7L, "AUDIT_LOG_VIEW"));

        assertThat(changed).isTrue();

        ArgumentCaptor<RecordAuditLogUseCase.RecordAuditLogCommand> captor =
                ArgumentCaptor.forClass(RecordAuditLogUseCase.RecordAuditLogCommand.class);
        verify(recordAuditLogUseCase).record(captor.capture());

        RecordAuditLogUseCase.RecordAuditLogCommand recorded = captor.getValue();
        assertThat(recorded.actionCode()).isEqualTo("PERMISSION_ASSIGN");
        assertThat(recorded.moduleKey()).isEqualTo("AUTH");
        assertThat(recorded.targetType()).isEqualTo("GROUP_PERMISSION");
        assertThat(recorded.targetId()).isEqualTo("7");
        assertThat(recorded.targetLabel()).isEqualTo("AUDIT_LOG_VIEW");
        assertThat(recorded.resultStatus()).isEqualTo(AuditResultStatus.SUCCESS);
        assertThat(recorded.description()).isEqualTo("Assigned permission AUDIT_LOG_VIEW to group 7");
        assertThat(recorded.errorMessage()).isNull();
        assertThat(recorded.actor().username()).isEqualTo("admin");
        assertThat(recorded.actor().groupName()).isEqualTo("ADMIN");
        assertThat(recorded.details()).isEqualTo(Map.of("groupId", 7L, "permissionCode", "AUDIT_LOG_VIEW"));
    }

    @Test
    void shouldRecordFailAuditLogAndRethrowOriginalException() {
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> proxy.assignPermissionWithFailure(new AssignPermissionCommand(9L, "ORDER_VIEW")));

        assertThat(exception).hasMessage("User group not found with id: 9");

        ArgumentCaptor<RecordAuditLogUseCase.RecordAuditLogCommand> captor =
                ArgumentCaptor.forClass(RecordAuditLogUseCase.RecordAuditLogCommand.class);
        verify(recordAuditLogUseCase).record(captor.capture());

        RecordAuditLogUseCase.RecordAuditLogCommand recorded = captor.getValue();
        assertThat(recorded.resultStatus()).isEqualTo(AuditResultStatus.FAIL);
        assertThat(recorded.description()).isEqualTo("Failed to assign permission ORDER_VIEW to group 9");
        assertThat(recorded.errorMessage()).isEqualTo("User group not found with id: 9");
        assertThat(recorded.targetId()).isEqualTo("9");
    }

    @Test
    void shouldNotBlockBusinessFlowWhenAuditWriteFails() {
        doThrow(new RuntimeException("audit db unavailable")).when(recordAuditLogUseCase).record(any());

        boolean changed = proxy.assignPermission(new AssignPermissionCommand(11L, "REPORT_VIEW"));

        assertThat(changed).isTrue();
    }

    public static class TestAuditService {

        @AuditAction(
                action = "PERMISSION_ASSIGN",
                module = "AUTH",
                targetType = "GROUP_PERMISSION",
                targetIdExpression = "#command.groupId",
                targetLabelExpression = "#command.permissionCode",
                successDescriptionExpression =
                        "#result ? 'Assigned permission ' + #command.permissionCode + ' to group ' + #command.groupId "
                                + ": 'Permission ' + #command.permissionCode + ' was already assigned to group ' + #command.groupId",
                failureDescriptionExpression =
                        "'Failed to assign permission ' + #command.permissionCode + ' to group ' + #command.groupId",
                detailsExpression = "#command")
        boolean assignPermission(AssignPermissionCommand command) {
            return true;
        }

        @AuditAction(
                action = "PERMISSION_ASSIGN",
                module = "AUTH",
                targetType = "GROUP_PERMISSION",
                targetIdExpression = "#command.groupId",
                targetLabelExpression = "#command.permissionCode",
                successDescriptionExpression =
                        "#result ? 'Assigned permission ' + #command.permissionCode + ' to group ' + #command.groupId "
                                + ": 'Permission ' + #command.permissionCode + ' was already assigned to group ' + #command.groupId",
                failureDescriptionExpression =
                        "'Failed to assign permission ' + #command.permissionCode + ' to group ' + #command.groupId",
                detailsExpression = "#command")
        boolean assignPermissionWithFailure(AssignPermissionCommand command) {
            throw new EntityNotFoundException("User group not found with id: " + command.groupId());
        }
    }
}
