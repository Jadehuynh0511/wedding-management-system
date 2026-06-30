package com.uit.weddingmanagement.modules.auth.application.model.result;

import java.time.Instant;

// Model này chứa token pair để trả về API sau khi đăng nhập thành công.
public record LoginResult(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        String refreshToken,
        Instant refreshExpiresAt) {
}
