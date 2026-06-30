package com.uit.weddingmanagement.modules.auth.domain.model;

public enum UserStatus {
    ACTIVE,
    INACTIVE,
    LOCKED;

    public boolean canAuthenticate() {
        // Trong phase 3, chỉ account ACTIVE mới được đăng nhập và dùng token.
        return this == ACTIVE;
    }
}
