package com.uit.weddingmanagement.modules.auth.application.usecase;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.auth.application.model.result.GroupPermissionResult;
import com.uit.weddingmanagement.modules.auth.application.port.in.GetGroupPermissionsUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupPermissionQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupQueryPort;

// Use case này lấy danh sách permission code mà group đang có để hiển thị ở màn hình quản trị
@Service
@Transactional(readOnly = true)
public class GetGroupPermissionsService implements GetGroupPermissionsUseCase {

    private final GroupQueryPort groupQueryPort;
    private final GroupPermissionQueryPort groupPermissionQueryPort;

    public GetGroupPermissionsService(
            GroupQueryPort groupQueryPort, GroupPermissionQueryPort groupPermissionQueryPort) {
        this.groupQueryPort = groupQueryPort;
        this.groupPermissionQueryPort = groupPermissionQueryPort;
    }

    @Override
    public GroupPermissionResult getGroupPermissions(Long groupId) {
        groupQueryPort.findGroupById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("User group not found with id: " + groupId));

        return new GroupPermissionResult(groupId, groupPermissionQueryPort.findPermissionCodesByGroupId(groupId));
    }
}
