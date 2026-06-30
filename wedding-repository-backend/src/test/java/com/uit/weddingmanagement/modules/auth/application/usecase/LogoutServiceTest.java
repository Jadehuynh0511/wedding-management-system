package com.uit.weddingmanagement.modules.auth.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uit.weddingmanagement.modules.auth.application.model.command.LogoutCommand;
import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.TokenProviderPort;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenRevocationReason;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenSession;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private RefreshTokenSessionQueryPort refreshTokenSessionQueryPort;

    @Mock
    private RefreshTokenSessionCommandPort refreshTokenSessionCommandPort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Test
    void shouldRevokeActiveFamilyWhenRefreshTokenExists() {
        UUID familyId = UUID.randomUUID();
        RefreshTokenSession refreshTokenSession = new RefreshTokenSession(
                20L,
                1L,
                familyId,
                "refresh-hash",
                Instant.now().plusSeconds(3600),
                null,
                null,
                null,
                null,
                "127.0.0.1",
                "Mozilla/5.0");

        when(tokenProviderPort.hashRefreshToken("refresh-token")).thenReturn("refresh-hash");
        when(refreshTokenSessionQueryPort.findByTokenHashForUpdate("refresh-hash"))
                .thenReturn(Optional.of(refreshTokenSession));

        LogoutService logoutService = new LogoutService(
                refreshTokenSessionQueryPort,
                refreshTokenSessionCommandPort,
                tokenProviderPort);

        logoutService.logout(new LogoutCommand(" refresh-token "));

        verify(refreshTokenSessionCommandPort).revokeActiveTokensByFamily(
                eq(1L),
                eq(familyId),
                any(Instant.class),
                eq(RefreshTokenRevocationReason.LOGGED_OUT));
    }

    @Test
    void shouldIgnoreBlankOrMissingRefreshToken() {
        when(tokenProviderPort.hashRefreshToken("unknown-token")).thenReturn("unknown-hash");
        when(refreshTokenSessionQueryPort.findByTokenHashForUpdate("unknown-hash")).thenReturn(Optional.empty());

        LogoutService logoutService = new LogoutService(
                refreshTokenSessionQueryPort,
                refreshTokenSessionCommandPort,
                tokenProviderPort);

        logoutService.logout(new LogoutCommand("   "));
        logoutService.logout(new LogoutCommand("unknown-token"));

        verify(refreshTokenSessionCommandPort, never()).revokeActiveTokensByFamily(any(), any(), any(), any());
    }
}
