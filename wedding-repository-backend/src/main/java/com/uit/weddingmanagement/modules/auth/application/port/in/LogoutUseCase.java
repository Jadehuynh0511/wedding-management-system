package com.uit.weddingmanagement.modules.auth.application.port.in;

import com.uit.weddingmanagement.modules.auth.application.model.command.LogoutCommand;

public interface LogoutUseCase {

    void logout(LogoutCommand command);
}
