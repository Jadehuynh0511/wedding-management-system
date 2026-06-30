package com.uit.weddingmanagement.modules.auth.application.port.in;

import com.uit.weddingmanagement.modules.auth.application.model.command.RevokePermissionCommand;

public interface RevokePermissionFromGroupUseCase {

    boolean revokePermissionFromGroup(RevokePermissionCommand command);
}
