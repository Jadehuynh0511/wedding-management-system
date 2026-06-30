package com.uit.weddingmanagement.modules.auth.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenSession;

public interface RefreshTokenSessionQueryPort {

    Optional<RefreshTokenSession> findByTokenHashForUpdate(String tokenHash);

    boolean existsActiveTokenInFamily(Long userId, UUID familyId, Instant asOf);
}
