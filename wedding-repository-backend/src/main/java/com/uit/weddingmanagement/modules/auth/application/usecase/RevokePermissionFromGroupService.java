package com.uit.weddingmanagement.modules.auth.application.usecase;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.auth.application.model.command.RevokePermissionCommand;
import com.uit.weddingmanagement.modules.auth.application.port.in.RevokePermissionFromGroupUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupPermissionCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupPermissionQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.PermissionQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.exception.AdminPrivilegeRequiredException;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.auth.domain.model.Permission;

// Use case thu hồi quyền khỏi group.
// Nếu mapping không tồn tại sẵn thì vẫn trả thành công để giữ tính idempotent
@Service
@Transactional
public class RevokePermissionFromGroupService implements RevokePermissionFromGroupUseCase {

    private final CurrentUserPort currentUserPort;
    private final GroupQueryPort groupQueryPort;
    private final PermissionQueryPort permissionQueryPort;
    private final GroupPermissionQueryPort groupPermissionQueryPort;
    private final GroupPermissionCommandPort groupPermissionCommandPort;

    public RevokePermissionFromGroupService(
            CurrentUserPort currentUserPort,
            GroupQueryPort groupQueryPort,
            PermissionQueryPort permissionQueryPort,
            GroupPermissionQueryPort groupPermissionQueryPort,
            GroupPermissionCommandPort groupPermissionCommandPort) {
        this.currentUserPort = currentUserPort;
        this.groupQueryPort = groupQueryPort;
        this.permissionQueryPort = permissionQueryPort;
        this.groupPermissionQueryPort = groupPermissionQueryPort;
        this.groupPermissionCommandPort = groupPermissionCommandPort;
    }

    @Override
    @AuditAction(
            action = "PERMISSION_REVOKE",
            module = "AUTH",
            targetType = "GROUP_PERMISSION",
            targetIdExpression = "#command.groupId",
            targetLabelExpression = "#command.permissionCode",
            successDescriptionExpression =
                    "#result ? 'Revoked permission ' + #command.permissionCode + ' from group ' + #command.groupId "
                            + ": 'Permission ' + #command.permissionCode + ' was already absent from group ' + #command.groupId",
            failureDescriptionExpression =
                    "'Failed to revoke permission ' + #command.permissionCode + ' from group ' + #command.groupId",
            detailsExpression = "#command")
    public boolean revokePermissionFromGroup(RevokePermissionCommand command) {
        ensureAdminPrivileges();

        Long groupId = requireGroupId(command.groupId());
        String permissionCode = normalizePermissionCode(command.permissionCode());

        groupQueryPort.findGroupById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("User group not found with id: " + groupId));

        Permission permission = permissionQueryPort.findPermissionByCode(permissionCode)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Permission not found with code: " + permissionCode));

        if (!groupPermissionQueryPort.existsByGroupIdAndPermissionId(groupId, permission.id())) {
            return false;
        }

        groupPermissionCommandPort.revokePermissionFromGroup(groupId, permission.id());
        return true;
    }

    private void ensureAdminPrivileges() {
        AuthenticatedUser currentUser = currentUserPort.getCurrentUser();

        if (!currentUser.isAdmin()) {
            throw new AdminPrivilegeRequiredException();
        }
    }

    private Long requireGroupId(Long groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("Group id is required.");
        }

        return groupId;
    }

    private String normalizePermissionCode(String permissionCode) {
        if (permissionCode == null || permissionCode.isBlank()) {
            throw new IllegalArgumentException("Permission code is required.");
        }

        return permissionCode.trim().toUpperCase();
    }
}
