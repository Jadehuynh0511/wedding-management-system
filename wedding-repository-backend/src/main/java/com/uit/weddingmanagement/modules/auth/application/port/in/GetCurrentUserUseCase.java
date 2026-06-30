package com.uit.weddingmanagement.modules.auth.application.port.in;

import com.uit.weddingmanagement.modules.auth.application.model.result.CurrentUserResult;

// Interface cho use case lấy thông tin user hiện tại, định nghĩa hành động getCurrentUser.
public interface GetCurrentUserUseCase {

    CurrentUserResult getCurrentUser();
}
