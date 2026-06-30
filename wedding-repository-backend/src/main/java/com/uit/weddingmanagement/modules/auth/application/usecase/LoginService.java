package com.uit.weddingmanagement.modules.auth.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.auth.application.model.internal.AccessToken;
import com.uit.weddingmanagement.modules.auth.application.model.command.LoginCommand;
import com.uit.weddingmanagement.modules.auth.application.model.result.LoginResult;
import com.uit.weddingmanagement.modules.auth.application.model.internal.RefreshToken;
import com.uit.weddingmanagement.modules.auth.application.port.in.LoginUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.PasswordHashPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.TokenProviderPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.exception.InvalidCredentialsException;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenSession;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;

// LoginService là use case trung tâm cho hành vi đăng nhập.
// Nó phối hợp domain models + ports, không truy cập DB/JWT/BCrypt trực tiếp.
@Service
@Transactional
public class LoginService implements LoginUseCase {

    private final UserAccountQueryPort userAccountQueryPort;
    private final PasswordHashPort passwordHashPort;
    private final TokenProviderPort tokenProviderPort;
    private final RefreshTokenSessionCommandPort refreshTokenSessionCommandPort;

    public LoginService(
            UserAccountQueryPort userAccountQueryPort,
            PasswordHashPort passwordHashPort,
            TokenProviderPort tokenProviderPort,
            RefreshTokenSessionCommandPort refreshTokenSessionCommandPort) {
        this.userAccountQueryPort = userAccountQueryPort;
        this.passwordHashPort = passwordHashPort;
        this.tokenProviderPort = tokenProviderPort;
        this.refreshTokenSessionCommandPort = refreshTokenSessionCommandPort;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        String normalizedUsername = command.username().trim();

        UserAccount userAccount = userAccountQueryPort.findByUsername(normalizedUsername)
                .orElseThrow(InvalidCredentialsException::new);

        userAccount.ensureCanAuthenticate();

        if (!passwordHashPort.matches(command.password(), userAccount.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        AuthenticatedUser authenticatedUser = userAccount.toAuthenticatedUser();
        UUID sessionFamilyId = UUID.randomUUID();
        RefreshToken refreshToken = tokenProviderPort.issueRefreshToken();
        AccessToken accessToken = tokenProviderPort.issueAccessToken(authenticatedUser, sessionFamilyId);

        refreshTokenSessionCommandPort.save(new RefreshTokenSession(
                null,
                authenticatedUser.id(),
                sessionFamilyId,
                refreshToken.hash(),
                refreshToken.expiresAt(),
                null,
                null,
                null,
                null,
                normalizeNullable(command.ipAddress(), 64),
                normalizeNullable(command.userAgent(), 512)));

        return new LoginResult(
                accessToken.value(),
                accessToken.tokenType(),
                accessToken.expiresAt(),
                refreshToken.value(),
                refreshToken.expiresAt());
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
