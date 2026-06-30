package com.uit.weddingmanagement.modules.auth.application.port.in;

import com.uit.weddingmanagement.modules.auth.application.model.command.CreateUserAccountCommand;
import com.uit.weddingmanagement.modules.auth.application.model.result.UserAccountResult;

public interface CreateUserAccountUseCase {

    UserAccountResult createUserAccount(CreateUserAccountCommand command);
}
