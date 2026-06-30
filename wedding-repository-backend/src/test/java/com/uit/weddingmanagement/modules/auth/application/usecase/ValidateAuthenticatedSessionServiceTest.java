package com.uit.weddingmanagement.modules.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.exception.InactiveAuthenticatedSessionException;

@ExtendWith(MockitoExtension.class)
class ValidateAuthenticatedSessionServiceTest {

    @Mock
    private RefreshTokenSessionQueryPort refreshTokenSessionQueryPort;

    @Test
    void shouldAllowRequestWhenSessionFamilyStillHasActiveToken() {
        UUID familyId = UUID.randomUUID();

        when(refreshTokenSessionQueryPort.existsActiveTokenInFamily(eq(1L), eq(familyId), any(Instant.class)))
                .thenReturn(true);

        ValidateAuthenticatedSessionService validateAuthenticatedSessionService =
                new ValidateAuthenticatedSessionService(refreshTokenSessionQueryPort);

        validateAuthenticatedSessionService.ensureSessionIsActive(1L, familyId);
    }

    @Test
    void shouldRejectRequestWhenSessionFamilyIsNoLongerActive() {
        UUID familyId = UUID.randomUUID();

        when(refreshTokenSessionQueryPort.existsActiveTokenInFamily(eq(1L), eq(familyId), any(Instant.class)))
                .thenReturn(false);

        ValidateAuthenticatedSessionService validateAuthenticatedSessionService =
                new ValidateAuthenticatedSessionService(refreshTokenSessionQueryPort);

        assertThatThrownBy(() -> validateAuthenticatedSessionService.ensureSessionIsActive(1L, familyId))
                .isInstanceOf(InactiveAuthenticatedSessionException.class);
    }
}
