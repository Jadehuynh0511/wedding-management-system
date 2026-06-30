package com.uit.weddingmanagement.modules.auth.application.usecase;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.auth.application.model.internal.AccessToken;
import com.uit.weddingmanagement.modules.auth.application.model.command.RefreshSessionCommand;
import com.uit.weddingmanagement.modules.auth.application.model.result.RefreshSessionResult;
import com.uit.weddingmanagement.modules.auth.application.model.internal.RefreshToken;
import com.uit.weddingmanagement.modules.auth.application.port.in.RefreshSessionUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.TokenProviderPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.exception.InvalidRefreshTokenException;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenRevocationReason;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenSession;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;

// Refresh session sẽ rotate refresh token để giảm rủi ro replay/reuse.
@Service
@Transactional
public class RefreshSessionService implements RefreshSessionUseCase {

    private final RefreshTokenSessionQueryPort refreshTokenSessionQueryPort;
    private final RefreshTokenSessionCommandPort refreshTokenSessionCommandPort;
    private final UserAccountQueryPort userAccountQueryPort;
    private final TokenProviderPort tokenProviderPort;

    public RefreshSessionService(
            RefreshTokenSessionQueryPort refreshTokenSessionQueryPort,
            RefreshTokenSessionCommandPort refreshTokenSessionCommandPort,
            UserAccountQueryPort userAccountQueryPort,
            TokenProviderPort tokenProviderPort) {
        this.refreshTokenSessionQueryPort = refreshTokenSessionQueryPort;
        this.refreshTokenSessionCommandPort = refreshTokenSessionCommandPort;
        this.userAccountQueryPort = userAccountQueryPort;
        this.tokenProviderPort = tokenProviderPort;
    }

    @Override
    public RefreshSessionResult refresh(RefreshSessionCommand command) {
        Instant now = Instant.now();
        String refreshTokenHash = tokenProviderPort.hashRefreshToken(command.refreshToken().trim());

        RefreshTokenSession currentSession = refreshTokenSessionQueryPort.findByTokenHashForUpdate(refreshTokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (currentSession.isRevoked()) {
            refreshTokenSessionCommandPort.revokeActiveTokensByFamily(
                    currentSession.userId(),
                    currentSession.familyId(),
                    now,
                    RefreshTokenRevocationReason.REUSE_DETECTED);
            throw new InvalidRefreshTokenException();
        }

        if (currentSession.isExpiredAt(now)) {
            throw new InvalidRefreshTokenException();
        }

        UserAccount userAccount = userAccountQueryPort.findById(currentSession.userId())
                .orElseThrow(InvalidRefreshTokenException::new);
        userAccount.ensureCanAuthenticate();

        AuthenticatedUser authenticatedUser = userAccount.toAuthenticatedUser();
        RefreshToken nextRefreshToken = tokenProviderPort.issueRefreshToken();
        AccessToken nextAccessToken = tokenProviderPort.issueAccessToken(authenticatedUser, currentSession.familyId());

        refreshTokenSessionCommandPort.save(currentSession.markRotated(nextRefreshToken.hash(), now));
        refreshTokenSessionCommandPort.save(new RefreshTokenSession(
                null,
                authenticatedUser.id(),
                currentSession.familyId(),
                nextRefreshToken.hash(),
                nextRefreshToken.expiresAt(),
                null,
                null,
                null,
                null,
                normalizeNullable(command.ipAddress(), 64),
                normalizeNullable(command.userAgent(), 512)));

        return new RefreshSessionResult(
                nextAccessToken.value(),
                nextAccessToken.tokenType(),
                nextAccessToken.expiresAt(),
                nextRefreshToken.value(),
                nextRefreshToken.expiresAt());
    }

    private String normalizeNullable(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String normalizedValue = value.trim();
        if (normalizedValue.isEmpty()) {
            return null;
        }

        if (normalizedValue.length() <= maxLength) {
            return normalizedValue;
        }

        return normalizedValue.substring(0, maxLength);
    }
}
