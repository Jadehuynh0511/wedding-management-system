package com.uit.weddingmanagement.modules.auth.application.usecase;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.auth.application.model.command.AssignPermissionCommand;
import com.uit.weddingmanagement.modules.auth.application.port.in.AssignPermissionToGroupUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupPermissionCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupPermissionQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.PermissionQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.exception.AdminPrivilegeRequiredException;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.auth.domain.model.Permission;

// Use case cấp quyền cho group
// Các rule quan trọng của use case này (chỉ ADMIN mới được phép gọi, group và permission phải tồn tại, idempotent)
// đều được giữ ở đây thay vì đẩy xuống controller để đảm bảo tính nhất quán và tái sử dụng logic ở nhiều chỗ khác nếu cần.
@Service
@Transactional
public class AssignPermissionToGroupService implements AssignPermissionToGroupUseCase {

    private final CurrentUserPort currentUserPort;
    private final GroupQueryPort groupQueryPort;
    private final PermissionQueryPort permissionQueryPort;
    private final GroupPermissionQueryPort groupPermissionQueryPort;
    private final GroupPermissionCommandPort groupPermissionCommandPort;

    public AssignPermissionToGroupService(
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

    // Gán một permission cho một group
    @Override
    @AuditAction(
            // Metadata cho audit log, sẽ được Aspect đọc và ghi lại mỗi khi phương thức này
            // được gọi
            action = "PERMISSION_ASSIGN", // Mã hành động
            module = "AUTH", // Module hoặc phạm vi của hành động
            targetType = "GROUP_PERMISSION", // Loại đối tượng bị tác động
            targetIdExpression = "#command.groupId", // Biểu thức SpEL để lấy ID của đối tượng bị tác động
            targetLabelExpression = "#command.permissionCode", // Biểu thức SpEL để lấy tên hoặc mô tả của đối tượng bị
                                                               // tác động
            successDescriptionExpression = "#result ? 'Assigned permission ' + #command.permissionCode + ' to group ' + #command.groupId "
                    + ": 'Permission ' + #command.permissionCode + ' was already assigned to group ' + #command.groupId", failureDescriptionExpression = "'Failed to assign permission ' + #command.permissionCode + ' to group ' + #command.groupId", detailsExpression = "#command")

    public boolean assignPermissionToGroup(AssignPermissionCommand command) {
        ensureAdminPrivileges();

        Long groupId = requireGroupId(command.groupId());
        String permissionCode = normalizePermissionCode(command.permissionCode());

        // Kiểm tra group và permission có tồn tại kh
        groupQueryPort.findGroupById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("User group not found with id: " + groupId));

        Permission permission = permissionQueryPort.findPermissionByCode(permissionCode)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Permission not found with code: " + permissionCode));

        // Nếu đã tồn tại mapping giữa group và permission thì không làm gì cả
        if (groupPermissionQueryPort.existsByGroupIdAndPermissionId(groupId, permission.id())) {
            return false;
        }

        // Nếu chưa tồn tại thì tạo mới mapping giữa group và permission để gán quyền
        // cho group
        groupPermissionCommandPort.assignPermissionToGroup(groupId, permission.id());
        return true;
    }

    // Đảm bảo đây là admin
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
