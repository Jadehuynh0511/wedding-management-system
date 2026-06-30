package com.uit.weddingmanagement.modules.auth.application.port.out;

public interface GroupPermissionCommandPort {

    void assignPermissionToGroup(Long groupId, Long permissionId);

    void revokePermissionFromGroup(Long groupId, Long permissionId);
}
