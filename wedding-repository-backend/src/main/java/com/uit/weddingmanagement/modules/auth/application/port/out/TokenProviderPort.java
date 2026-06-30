package com.uit.weddingmanagement.modules.auth.application.port.out;

import java.util.UUID;

import com.uit.weddingmanagement.modules.auth.application.model.internal.AccessToken;
import com.uit.weddingmanagement.modules.auth.application.model.internal.RefreshToken;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;

// Port này tách use case ra khỏi công nghệ JWT và chi tiết phát hành refresh token.
public interface TokenProviderPort {

    AccessToken issueAccessToken(AuthenticatedUser user, UUID sessionFamilyId);

    RefreshToken issueRefreshToken();

    String hashRefreshToken(String rawRefreshToken);

    TokenSubject parseAccessToken(String token);

    record TokenSubject(Long userId, String username, UUID sessionFamilyId) {
    }
}
