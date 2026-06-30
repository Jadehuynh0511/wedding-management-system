package com.uit.weddingmanagement.modules.auth.application.port.in;

import com.uit.weddingmanagement.modules.auth.application.model.result.UserAccountResult;

public interface GetUserAccountUseCase {

    UserAccountResult getUserAccount(Long userId);
}
