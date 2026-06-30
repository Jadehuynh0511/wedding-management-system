package com.uit.weddingmanagement.modules.auth.application.port.in;

import com.uit.weddingmanagement.modules.auth.application.model.command.AssignPermissionCommand;

// Khai báo Use case cấp quyền cho group
public interface AssignPermissionToGroupUseCase {

    boolean assignPermissionToGroup(AssignPermissionCommand command);
}
