package com.uit.weddingmanagement.modules.auth.application.model.result;

import java.time.Instant;

// Kết quả refresh session có cùng shape với login vì đều trả token pair mới.
public record RefreshSessionResult(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        String refreshToken,
        Instant refreshExpiresAt) {
}
