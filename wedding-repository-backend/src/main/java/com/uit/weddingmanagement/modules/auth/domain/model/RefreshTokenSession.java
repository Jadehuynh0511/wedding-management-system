package com.uit.weddingmanagement.modules.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

// Domain model đại diện cho một refresh token đã được phát hành.
public record RefreshTokenSession(
        Long id,
        Long userId,
        UUID familyId, // Một familyId dùng để nhóm các refresh token liên quan đến cùng một session
                       // (ví dụ: cùng một lần đăng nhập).
        String tokenHash,
        Instant expiresAt,
        Instant lastUsedAt,
        Instant revokedAt,
        RefreshTokenRevocationReason revokedReason,
        String replacedByTokenHash,
        String issuedIp,
        String issuedUserAgent) {

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpiredAt(Instant instant) {
        return !expiresAt.isAfter(instant);
    }

    public boolean isActiveAt(Instant instant) {
        return !isRevoked() && !isExpiredAt(instant);
    }

    // Khi refresh thành công, refresh token cũ sẽ được rotate (đánh dấu revoked và
    // lưu hash của refresh token mới thay thế).
    public RefreshTokenSession markRotated(String nextTokenHash, Instant rotatedAt) {
        return new RefreshTokenSession(
                id,
                userId,
                familyId,
                tokenHash,
                expiresAt,
                rotatedAt,
                rotatedAt,
                RefreshTokenRevocationReason.ROTATED,
                nextTokenHash,
                issuedIp,
                issuedUserAgent);
    }
}
