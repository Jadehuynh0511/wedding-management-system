package com.uit.weddingmanagement.modules.auth;

import java.util.Set;

import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.auth.domain.model.Permission;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;
import com.uit.weddingmanagement.modules.auth.domain.model.UserGroup;
import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

public final class AuthTestFixture {

    private AuthTestFixture() {
    }

    public static UserAccount activeAdminAccount() {
        return new UserAccount(
                1L,
                "admin",
                "$2a$10$hash",
                "Local Admin",
                "admin@local.dev",
                "0123456789",
                UserStatus.ACTIVE,
                adminGroup());
    }

    public static AuthenticatedUser authenticatedAdmin() {
        return activeAdminAccount().toAuthenticatedUser();
    }

    public static UserGroup adminGroup() {
        return new UserGroup(
                1L,
                "ADMIN",
                true,
                "System administration group",
                Set.of(permission("AUDIT_LOG_VIEW"), permission("USER_GROUP_MANAGE")));
    }

    public static Permission permission(String code) {
        return new Permission(1L, code, code, "SYSTEM", "HE_THONG", "Test permission");
    }
}
