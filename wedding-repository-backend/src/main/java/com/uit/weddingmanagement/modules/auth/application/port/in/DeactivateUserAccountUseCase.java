package com.uit.weddingmanagement.modules.auth.application.port.in;

import com.uit.weddingmanagement.modules.auth.application.model.result.UserAccountResult;

public interface DeactivateUserAccountUseCase {

    UserAccountResult deactivateUserAccount(Long userId);
}
