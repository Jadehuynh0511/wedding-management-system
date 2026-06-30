package com.uit.weddingmanagement.modules.auth.application.port.in;

import com.uit.weddingmanagement.modules.auth.application.model.command.RefreshSessionCommand;
import com.uit.weddingmanagement.modules.auth.application.model.result.RefreshSessionResult;

public interface RefreshSessionUseCase {

    RefreshSessionResult refresh(RefreshSessionCommand command);
}
