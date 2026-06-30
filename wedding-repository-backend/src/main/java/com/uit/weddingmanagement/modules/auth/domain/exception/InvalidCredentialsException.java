package com.uit.weddingmanagement.modules.auth.domain.exception;

// Ngoại lệ được dùng khi username hoặc password không chính xác.
public class InvalidCredentialsException extends RuntimeException {

    // Hàm khởi tạo mặc định
    public InvalidCredentialsException() {
        super("Invalid username or password.");
    }
}
