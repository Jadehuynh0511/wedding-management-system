package com.uit.weddingmanagement.modules.auth.application.model.command;

import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

public record UpdateUserAccountCommand(
        String username,
        String fullName,
        String email,
        String phoneNumber,
        Long groupId,
        UserStatus status) {}
