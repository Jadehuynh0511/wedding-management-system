package com.uit.weddingmanagement.modules.auth.application.port.out;

import java.time.Instant;
import java.util.UUID;

import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenRevocationReason;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenSession;

public interface RefreshTokenSessionCommandPort {

    void save(RefreshTokenSession refreshTokenSession);

    void revokeActiveTokensByFamily(Long userId, UUID familyId, Instant revokedAt, RefreshTokenRevocationReason reason);
}
