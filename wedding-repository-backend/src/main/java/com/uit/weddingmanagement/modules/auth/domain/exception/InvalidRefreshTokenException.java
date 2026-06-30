package com.uit.weddingmanagement.modules.auth.domain.exception;

// Refresh token sai, hết hạn, hoặc đã bị revoke đều trả về cùng một lỗi generic.
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token is invalid or expired.");
    }
}
