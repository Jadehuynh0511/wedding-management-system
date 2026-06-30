package com.uit.weddingmanagement.modules.auth.application.port.in;

import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;

// Interface cho use case lấy thông tin user hiện tại từ access token đã được xác thực.
public interface ResolveAuthenticatedUserUseCase {

    AuthenticatedUser resolveById(Long userId);
}
