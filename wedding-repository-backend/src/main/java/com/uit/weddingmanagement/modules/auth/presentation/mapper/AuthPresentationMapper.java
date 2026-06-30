package com.uit.weddingmanagement.modules.auth.presentation.mapper;

import com.uit.weddingmanagement.modules.auth.application.model.result.CurrentUserResult;
import com.uit.weddingmanagement.modules.auth.application.model.result.LoginResult;
import com.uit.weddingmanagement.modules.auth.application.model.result.RefreshSessionResult;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.CurrentUserResponse;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.LoginResponse;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.RefreshTokenResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi result của usecase-layer sang presentation-layer response
 * cho các API authentication (login, refresh, logout, /me).
 * Lý do cần Mapper là vì Presentation-layer chỉ có trách nhiệm nhận request từ
 * client và trả về response cho client.
 * Nó không nên biết về các DTOs và internal types của usecase-layer.
 * Việc dùng mapper để tách biệt 2 layer giúp dễ dàng thay đổi implementation
 * của một layer mà không ảnh hưởng đến layer khác.
 */
@Component
public class AuthPresentationMapper {

    // Mapper chuyển đổi LoginResult sang LoginResponse.
    public LoginResponse toResponse(LoginResult loginResult) {
        return new LoginResponse(
                loginResult.accessToken(),
                loginResult.tokenType(),
                loginResult.expiresAt(),
                loginResult.refreshToken(),
                loginResult.refreshExpiresAt());
    }

    // Mapper chuyển đổi RefreshSessionResult sang RefreshTokenResponse.
    public RefreshTokenResponse toResponse(RefreshSessionResult refreshSessionResult) {
        return new RefreshTokenResponse(
                refreshSessionResult.accessToken(),
                refreshSessionResult.tokenType(),
                refreshSessionResult.expiresAt(),
                refreshSessionResult.refreshToken(),
                refreshSessionResult.refreshExpiresAt());
    }

    // Mapper chuyển đổi CurrentUserResult sang CurrentUserResponse.
    public CurrentUserResponse toResponse(CurrentUserResult currentUserResult) {
        return new CurrentUserResponse(
                currentUserResult.id(),
                currentUserResult.username(),
                currentUserResult.groupName(),
                currentUserResult.permissionCodes());
    }
}
