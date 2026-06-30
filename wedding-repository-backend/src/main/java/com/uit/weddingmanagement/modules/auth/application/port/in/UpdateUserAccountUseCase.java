package com.uit.weddingmanagement.modules.auth.application.port.in;

import com.uit.weddingmanagement.modules.auth.application.model.command.UpdateUserAccountCommand;
import com.uit.weddingmanagement.modules.auth.application.model.result.UserAccountResult;

public interface UpdateUserAccountUseCase {

    UserAccountResult updateUserAccount(Long userId, UpdateUserAccountCommand command);
}
