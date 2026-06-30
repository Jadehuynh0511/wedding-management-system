package com.uit.weddingmanagement.modules.auth.application.usecase;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.auth.application.port.in.ValidateAuthenticatedSessionUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.exception.InactiveAuthenticatedSessionException;

// Access token chỉ hợp lệ khi session family tương ứng vẫn còn active ở DB.
@Service
@Transactional(readOnly = true)
public class ValidateAuthenticatedSessionService implements ValidateAuthenticatedSessionUseCase {

    private final RefreshTokenSessionQueryPort refreshTokenSessionQueryPort;

    public ValidateAuthenticatedSessionService(RefreshTokenSessionQueryPort refreshTokenSessionQueryPort) {
        this.refreshTokenSessionQueryPort = refreshTokenSessionQueryPort;
    }

    @Override
    public void ensureSessionIsActive(Long userId, UUID sessionFamilyId) {
        if (!refreshTokenSessionQueryPort.existsActiveTokenInFamily(userId, sessionFamilyId, Instant.now())) {
            throw new InactiveAuthenticatedSessionException();
        }
    }
}
