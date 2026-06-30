package com.uit.weddingmanagement.modules.auth.application.model.internal;

import java.time.Instant;

// Refresh token trả về cho client dưới dạng raw token, nhưng DB chỉ lưu hash.
public record RefreshToken(String value, String hash, Instant expiresAt) {
}
