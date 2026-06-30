package com.uit.weddingmanagement.modules.auth.application.port.in;

import com.uit.weddingmanagement.modules.auth.application.model.command.LoginCommand;
import com.uit.weddingmanagement.modules.auth.application.model.result.LoginResult;

// Interface cho use case login, định nghĩa hành động login.
public interface LoginUseCase {

    LoginResult login(LoginCommand command);
}
