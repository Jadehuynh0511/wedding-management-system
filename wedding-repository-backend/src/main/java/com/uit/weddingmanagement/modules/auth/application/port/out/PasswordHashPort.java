package com.uit.weddingmanagement.modules.auth.application.port.out;

// Port này là cầu nối từ application layer sang thư viện xử lý password hash (Bcrypt/Scrypt).
// Nhờ vậy use case login không phụ thuộc trực tiếp vào thư viện nào.
public interface PasswordHashPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
