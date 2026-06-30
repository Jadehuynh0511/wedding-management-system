package com.uit.weddingmanagement.modules.auth.application.port.out;

import java.util.Set;

public interface GroupPermissionQueryPort {

    Set<String> findPermissionCodesByGroupId(Long groupId);

    boolean existsByGroupIdAndPermissionId(Long groupId, Long permissionId);
}
