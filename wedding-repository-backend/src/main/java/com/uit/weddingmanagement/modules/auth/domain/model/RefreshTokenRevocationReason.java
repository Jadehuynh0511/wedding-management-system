package com.uit.weddingmanagement.modules.auth.domain.model;

// Lý do một refresh token bị revoke, dùng để phân biệt giữa các trường hợp như rotate, logout, hoặc reuse detection.
public enum RefreshTokenRevocationReason {
    // Refresh Token đã được rotate (refresh thành công và Refresh Token cũ bị
    // revoke).
    ROTATED,
    // Refresh Token bị revoke khi người dùng đăng xuất.
    LOGGED_OUT,
    // Refresh Token bị revoke khi phát hiện tái sử dụng.
    REUSE_DETECTED
}
