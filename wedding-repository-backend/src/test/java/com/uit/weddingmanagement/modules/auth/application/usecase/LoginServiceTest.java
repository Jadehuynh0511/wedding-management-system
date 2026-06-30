package com.uit.weddingmanagement.modules.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uit.weddingmanagement.modules.auth.AuthTestFixture;
import com.uit.weddingmanagement.modules.auth.application.model.command.LoginCommand;
import com.uit.weddingmanagement.modules.auth.application.model.internal.AccessToken;
import com.uit.weddingmanagement.modules.auth.application.model.internal.RefreshToken;
import com.uit.weddingmanagement.modules.auth.application.model.result.LoginResult;
import com.uit.weddingmanagement.modules.auth.application.port.out.PasswordHashPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.TokenProviderPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.exception.InvalidCredentialsException;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenSession;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserAccountQueryPort userAccountQueryPort;

    @Mock
    private PasswordHashPort passwordHashPort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private RefreshTokenSessionCommandPort refreshTokenSessionCommandPort;

    @Captor
    private ArgumentCaptor<RefreshTokenSession> refreshTokenSessionCaptor;

    @Test
    void shouldIssueTokenPairAndPersistRefreshSessionWhenCredentialsAreValid() {
        UserAccount userAccount = AuthTestFixture.activeAdminAccount();
        Instant accessExpiresAt = Instant.parse("2026-05-25T11:18:33Z");
        Instant refreshExpiresAt = Instant.parse("2026-06-24T11:03:33Z");
        AccessToken accessToken = new AccessToken("access-token", "Bearer", accessExpiresAt);
        RefreshToken refreshToken = new RefreshToken("refresh-token", "refresh-token-hash", refreshExpiresAt);

        when(userAccountQueryPort.findByUsername("admin")).thenReturn(Optional.of(userAccount));
        when(passwordHashPort.matches("admin123!", userAccount.passwordHash())).thenReturn(true);
        when(tokenProviderPort.issueRefreshToken()).thenReturn(refreshToken);
        when(tokenProviderPort.issueAccessToken(eq(userAccount.toAuthenticatedUser()), any(UUID.class)))
                .thenReturn(accessToken);

        LoginService loginService = new LoginService(
                userAccountQueryPort,
                passwordHashPort,
                tokenProviderPort,
                refreshTokenSessionCommandPort);

        LoginResult loginResult = loginService.login(new LoginCommand(
                "  admin  ",
                "admin123!",
                " 127.0.0.1 ",
                "  Mozilla/5.0 ".repeat(60)));

        assertThat(loginResult.accessToken()).isEqualTo("access-token");
        assertThat(loginResult.refreshToken()).isEqualTo("refresh-token");
        assertThat(loginResult.expiresAt()).isEqualTo(accessExpiresAt);
        assertThat(loginResult.refreshExpiresAt()).isEqualTo(refreshExpiresAt);

        verify(refreshTokenSessionCommandPort).save(refreshTokenSessionCaptor.capture());
        RefreshTokenSession persistedSession = refreshTokenSessionCaptor.getValue();
        assertThat(persistedSession.id()).isNull();
        assertThat(persistedSession.userId()).isEqualTo(userAccount.id());
        assertThat(persistedSession.familyId()).isNotNull();
        assertThat(persistedSession.tokenHash()).isEqualTo("refresh-token-hash");
        assertThat(persistedSession.expiresAt()).isEqualTo(refreshExpiresAt);
        assertThat(persistedSession.issuedIp()).isEqualTo("127.0.0.1");
        assertThat(persistedSession.issuedUserAgent()).hasSize(512);
        assertThat(persistedSession.revokedAt()).isNull();
    }

    @Test
    void shouldRejectLoginWhenPasswordDoesNotMatch() {
        UserAccount userAccount = AuthTestFixture.activeAdminAccount();

        when(userAccountQueryPort.findByUsername("admin")).thenReturn(Optional.of(userAccount));
        when(passwordHashPort.matches("wrong-password", userAccount.passwordHash())).thenReturn(false);

        LoginService loginService = new LoginService(
                userAccountQueryPort,
                passwordHashPort,
                tokenProviderPort,
                refreshTokenSessionCommandPort);

        assertThatThrownBy(() -> loginService.login(new LoginCommand("admin", "wrong-password", null, null)))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(tokenProviderPort, never()).issueRefreshToken();
        verify(tokenProviderPort, never()).issueAccessToken(any(), any(UUID.class));
        verify(refreshTokenSessionCommandPort, never()).save(any());
    }
}
