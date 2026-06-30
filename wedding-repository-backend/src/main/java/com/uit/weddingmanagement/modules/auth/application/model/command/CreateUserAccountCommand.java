package com.uit.weddingmanagement.modules.auth.application.model.command;

import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

public record CreateUserAccountCommand(
        String username,
        String password,
        String fullName,
        String email,
        String phoneNumber,
        Long groupId,
        UserStatus status) {}
