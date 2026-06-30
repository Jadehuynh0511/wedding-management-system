package com.uit.weddingmanagement.modules.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.uit.weddingmanagement.modules.auth.AuthTestFixture;
import com.uit.weddingmanagement.modules.auth.application.model.internal.RefreshToken;
import com.uit.weddingmanagement.modules.auth.application.port.out.TokenProviderPort;

class JwtTokenProviderAdapterTest {

    @Test
    void shouldIssueAndParseAccessTokenWithSessionFamilyId() {
        JwtTokenProviderAdapter jwtTokenProviderAdapter = createTokenProvider();
        UUID sessionFamilyId = UUID.randomUUID();

        TokenProviderPort.TokenSubject tokenSubject = jwtTokenProviderAdapter.parseAccessToken(
                jwtTokenProviderAdapter.issueAccessToken(AuthTestFixture.authenticatedAdmin(), sessionFamilyId).value());

        assertThat(tokenSubject.userId()).isEqualTo(1L);
        assertThat(tokenSubject.username()).isEqualTo("admin");
        assertThat(tokenSubject.sessionFamilyId()).isEqualTo(sessionFamilyId);
    }

    @Test
    void shouldIssueOpaqueRefreshTokenAndStableHash() {
        JwtTokenProviderAdapter jwtTokenProviderAdapter = createTokenProvider();

        RefreshToken refreshToken = jwtTokenProviderAdapter.issueRefreshToken();

        assertThat(refreshToken.value()).isNotBlank();
        assertThat(refreshToken.hash()).hasSize(64);
        assertThat(jwtTokenProviderAdapter.hashRefreshToken(refreshToken.value())).isEqualTo(refreshToken.hash());
        assertThat(refreshToken.expiresAt()).isAfter(java.time.Instant.now());
    }

    @Test
    void shouldRejectMalformedAccessToken() {
        JwtTokenProviderAdapter jwtTokenProviderAdapter = createTokenProvider();

        assertThatThrownBy(() -> jwtTokenProviderAdapter.parseAccessToken("not-a-jwt"))
                .isInstanceOf(JwtAuthenticationException.class);
    }

    private JwtTokenProviderAdapter createTokenProvider() {
        JwtTokenProviderAdapter jwtTokenProviderAdapter = new JwtTokenProviderAdapter(new AuthSecurityProperties(
                "change-this-dev-jwt-secret-for-wedding-management-2026",
                Duration.ofMinutes(15),
                Duration.ofDays(30)));
        jwtTokenProviderAdapter.initializeSigningKey();
        return jwtTokenProviderAdapter;
    }
}
