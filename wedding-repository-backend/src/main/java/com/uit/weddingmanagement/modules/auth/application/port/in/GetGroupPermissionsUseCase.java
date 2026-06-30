package com.uit.weddingmanagement.modules.auth.application.port.in;

import com.uit.weddingmanagement.modules.auth.application.model.result.GroupPermissionResult;

public interface GetGroupPermissionsUseCase {

    GroupPermissionResult getGroupPermissions(Long groupId);
}
