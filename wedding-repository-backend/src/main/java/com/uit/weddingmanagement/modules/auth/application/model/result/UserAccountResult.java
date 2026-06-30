package com.uit.weddingmanagement.modules.auth.application.model.result;

import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;
import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

public record UserAccountResult(
        Long id,
        String username,
        String fullName,
        String email,
        String phoneNumber,
        UserStatus status,
        Long groupId,
        String groupName) {

    public static UserAccountResult from(UserAccount userAccount) {
        return new UserAccountResult(
                userAccount.id(),
                userAccount.username(),
                userAccount.fullName(),
                userAccount.email(),
                userAccount.phoneNumber(),
                userAccount.status(),
                userAccount.userGroup().id(),
                userAccount.userGroup().name());
    }
}
