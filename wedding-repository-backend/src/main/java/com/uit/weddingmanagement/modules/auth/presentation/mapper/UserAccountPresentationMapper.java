package com.uit.weddingmanagement.modules.auth.presentation.mapper;

import com.uit.weddingmanagement.modules.auth.application.model.result.UserAccountResult;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.UserAccountResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi application-layer result sang presentation-layer response
 * cho các API quản lý tài khoản người dùng.
 */
@Component
public class UserAccountPresentationMapper {

    public UserAccountResponse toResponse(UserAccountResult userAccountResult) {
        return new UserAccountResponse(
                userAccountResult.id(),
                userAccountResult.username(),
                userAccountResult.fullName(),
                userAccountResult.email(),
                userAccountResult.phoneNumber(),
                userAccountResult.status(),
                userAccountResult.groupId(),
                userAccountResult.groupName());
    }
}
