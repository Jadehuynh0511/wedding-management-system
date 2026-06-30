package com.uit.weddingmanagement.modules.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uit.weddingmanagement.modules.auth.AuthTestFixture;
import com.uit.weddingmanagement.modules.auth.application.model.command.RefreshSessionCommand;
import com.uit.weddingmanagement.modules.auth.application.model.internal.AccessToken;
import com.uit.weddingmanagement.modules.auth.application.model.internal.RefreshToken;
import com.uit.weddingmanagement.modules.auth.application.model.result.RefreshSessionResult;
import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.TokenProviderPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.exception.InvalidRefreshTokenException;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenRevocationReason;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenSession;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;

@ExtendWith(MockitoExtension.class)
class RefreshSessionServiceTest {

    @Mock
    private RefreshTokenSessionQueryPort refreshTokenSessionQueryPort;

    @Mock
    private RefreshTokenSessionCommandPort refreshTokenSessionCommandPort;

    @Mock
    private UserAccountQueryPort userAccountQueryPort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Captor
    private ArgumentCaptor<RefreshTokenSession> refreshTokenSessionCaptor;

    @Test
    void shouldRotateRefreshTokenAndIssueNewTokenPair() {
        UserAccount userAccount = AuthTestFixture.activeAdminAccount();
        UUID familyId = UUID.randomUUID();
        RefreshTokenSession currentSession = new RefreshTokenSession(
                10L,
                userAccount.id(),
                familyId,
                "current-hash",
                Instant.now().plusSeconds(3600),
                null,
                null,
                null,
                null,
                "127.0.0.1",
                "Mozilla/5.0");
        RefreshToken nextRefreshToken = new RefreshToken(
                "next-refresh-token",
                "next-refresh-token-hash",
                Instant.parse("2026-06-24T11:03:33Z"));
        AccessToken nextAccessToken = new AccessToken(
                "next-access-token",
                "Bearer",
                Instant.parse("2026-05-25T11:18:33Z"));

        when(tokenProviderPort.hashRefreshToken("raw-refresh-token")).thenReturn("current-hash");
        when(refreshTokenSessionQueryPort.findByTokenHashForUpdate("current-hash"))
                .thenReturn(Optional.of(currentSession));
        when(userAccountQueryPort.findById(userAccount.id())).thenReturn(Optional.of(userAccount));
        when(tokenProviderPort.issueRefreshToken()).thenReturn(nextRefreshToken);
        when(tokenProviderPort.issueAccessToken(eq(userAccount.toAuthenticatedUser()), eq(familyId)))
                .thenReturn(nextAccessToken);

        RefreshSessionService refreshSessionService = new RefreshSessionService(
                refreshTokenSessionQueryPort,
                refreshTokenSessionCommandPort,
                userAccountQueryPort,
                tokenProviderPort);

        RefreshSessionResult refreshSessionResult = refreshSessionService.refresh(new RefreshSessionCommand(
                " raw-refresh-token ",
                " 10.10.10.10 ",
                "  Mozilla/5.0 "));

        assertThat(refreshSessionResult.accessToken()).isEqualTo("next-access-token");
        assertThat(refreshSessionResult.refreshToken()).isEqualTo("next-refresh-token");
        assertThat(refreshSessionResult.refreshExpiresAt()).isEqualTo(nextRefreshToken.expiresAt());

        verify(refreshTokenSessionCommandPort, times(2)).save(refreshTokenSessionCaptor.capture());

        List<RefreshTokenSession> persistedSessions = refreshTokenSessionCaptor.getAllValues();
        RefreshTokenSession rotatedSession = persistedSessions.get(0);
        RefreshTokenSession newSession = persistedSessions.get(1);

        assertThat(rotatedSession.id()).isEqualTo(currentSession.id());
        assertThat(rotatedSession.revokedReason()).isEqualTo(RefreshTokenRevocationReason.ROTATED);
        assertThat(rotatedSession.replacedByTokenHash()).isEqualTo("next-refresh-token-hash");
        assertThat(rotatedSession.revokedAt()).isNotNull();
        assertThat(rotatedSession.lastUsedAt()).isNotNull();

        assertThat(newSession.id()).isNull();
        assertThat(newSession.familyId()).isEqualTo(familyId);
        assertThat(newSession.tokenHash()).isEqualTo("next-refresh-token-hash");
        assertThat(newSession.issuedIp()).isEqualTo("10.10.10.10");
        assertThat(newSession.issuedUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(newSession.revokedAt()).isNull();
    }

    @Test
    void shouldRevokeFamilyAndRejectWhenRefreshTokenWasAlreadyRevoked() {
        UserAccount userAccount = AuthTestFixture.activeAdminAccount();
        UUID familyId = UUID.randomUUID();
        RefreshTokenSession revokedSession = new RefreshTokenSession(
                11L,
                userAccount.id(),
                familyId,
                "revoked-hash",
                Instant.now().plusSeconds(3600),
                Instant.now().minusSeconds(30),
                Instant.now().minusSeconds(30),
                RefreshTokenRevocationReason.ROTATED,
                "next-hash",
                "127.0.0.1",
                "Mozilla/5.0");

        when(tokenProviderPort.hashRefreshToken("revoked-token")).thenReturn("revoked-hash");
        when(refreshTokenSessionQueryPort.findByTokenHashForUpdate("revoked-hash"))
                .thenReturn(Optional.of(revokedSession));

        RefreshSessionService refreshSessionService = new RefreshSessionService(
                refreshTokenSessionQueryPort,
                refreshTokenSessionCommandPort,
                userAccountQueryPort,
                tokenProviderPort);

        assertThatThrownBy(() -> refreshSessionService.refresh(new RefreshSessionCommand(
                "revoked-token",
                null,
                null)))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenSessionCommandPort).revokeActiveTokensByFamily(
                eq(userAccount.id()),
                eq(familyId),
                any(Instant.class),
                eq(RefreshTokenRevocationReason.REUSE_DETECTED));
        verify(refreshTokenSessionCommandPort, never()).save(any());
    }
}
