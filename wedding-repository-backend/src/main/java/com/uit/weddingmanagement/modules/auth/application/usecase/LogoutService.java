package com.uit.weddingmanagement.modules.auth.application.usecase;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.auth.application.model.command.LogoutCommand;
import com.uit.weddingmanagement.modules.auth.application.port.in.LogoutUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.TokenProviderPort;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenRevocationReason;

// Logout là thao tác idempotent: nếu token không còn tồn tại thì vẫn trả success.
@Service
@Transactional
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenSessionQueryPort refreshTokenSessionQueryPort;
    private final RefreshTokenSessionCommandPort refreshTokenSessionCommandPort;
    private final TokenProviderPort tokenProviderPort;

    public LogoutService(
            RefreshTokenSessionQueryPort refreshTokenSessionQueryPort,
            RefreshTokenSessionCommandPort refreshTokenSessionCommandPort,
            TokenProviderPort tokenProviderPort) {
        this.refreshTokenSessionQueryPort = refreshTokenSessionQueryPort;
        this.refreshTokenSessionCommandPort = refreshTokenSessionCommandPort;
        this.tokenProviderPort = tokenProviderPort;
    }

    // Khi logout, revoke toàn bộ family của refresh token đó để đảm bảo các refresh
    // token còn hoạt động khác trong cùng family cũng bị revoke, đồng thời làm cho
    // các access token còn hoạt động liên quan đến family đó cũng trở nên vô hiệu
    // (do kiểm tra familyId trong access token validation). Cách này giúp đảm bảo
    // rằng khi người dùng logout hoặc khi phát hiện tái sử dụng refresh token, tất
    // cả các token (access token và refresh token) liên quan đến session đó đều bị
    // revoke, không cho phép tiếp tục
    // sử dụng nữa.
    @Override
    public void logout(LogoutCommand command) {
        String rawRefreshToken = command.refreshToken().trim();
        if (rawRefreshToken.isEmpty()) {
            return;
        }

        String refreshTokenHash = tokenProviderPort.hashRefreshToken(rawRefreshToken);
        refreshTokenSessionQueryPort.findByTokenHashForUpdate(refreshTokenHash)
                .ifPresent(refreshTokenSession -> refreshTokenSessionCommandPort.revokeActiveTokensByFamily(
                        refreshTokenSession.userId(),
                        refreshTokenSession.familyId(),
                        Instant.now(),
                        RefreshTokenRevocationReason.LOGGED_OUT));
    }
}
