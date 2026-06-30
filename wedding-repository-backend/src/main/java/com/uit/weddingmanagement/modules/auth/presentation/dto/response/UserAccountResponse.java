package com.uit.weddingmanagement.modules.auth.presentation.dto.response;

import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

public record UserAccountResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phoneNumber,
        UserStatus status,
        Long groupId,
        String groupName) {}
