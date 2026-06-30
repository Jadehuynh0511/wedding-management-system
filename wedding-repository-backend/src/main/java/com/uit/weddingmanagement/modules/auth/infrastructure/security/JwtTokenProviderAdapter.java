package com.uit.weddingmanagement.modules.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.uit.weddingmanagement.modules.auth.application.model.internal.AccessToken;
import com.uit.weddingmanagement.modules.auth.application.model.internal.RefreshToken;
import com.uit.weddingmanagement.modules.auth.application.port.out.TokenProviderPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;

// Adapter chịu trách nhiệm phát hành token và parse access token JWT.
// Đây là nơi toàn bộ chi tiết thư viện jjwt và crypto được giữ lại.
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    // Độ dài byte của refresh token raw trước khi được encode thành string
    private static final int REFRESH_TOKEN_BYTE_LENGTH = 32;

    private final AuthSecurityProperties authSecurityProperties;
    // SecureRandom để tạo refresh token ngẫu nhiên, đảm bảo tính bảo mật và không
    // thể đoán trước.
    private final SecureRandom secureRandom = new SecureRandom();
    // Signing key để ký và verify JWT
    private javax.crypto.SecretKey signingKey;

    public JwtTokenProviderAdapter(AuthSecurityProperties authSecurityProperties) {
        this.authSecurityProperties = authSecurityProperties;
    }

    @PostConstruct
    void initializeSigningKey() {
        try {
            this.signingKey = Keys.hmacShaKeyFor(authSecurityProperties.secret().getBytes(StandardCharsets.UTF_8));
        } catch (WeakKeyException exception) {
            throw new IllegalStateException("JWT secret must be long enough for HS256 signing.", exception);
        }
    }

    // Issue một access token
    @Override
    public AccessToken issueAccessToken(AuthenticatedUser user, UUID sessionFamilyId) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(authSecurityProperties.accessTokenTtl());

        String tokenValue = Jwts.builder()
                .subject(user.username())
                .claim("userId", user.id())
                // Lưu sessionFamilyId trong claim để có thể kiểm tra trong quá trình xác thực
                // access token, đảm bảo rằng token đó liên quan đến một session family vẫn còn
                // hoạt động (chưa bị revoke)
                .claim("sessionFamilyId", sessionFamilyId.toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();

        return new AccessToken(tokenValue, "Bearer", expiresAt);
    }

    // Issue một refresh token mới. Refresh token được tạo ngẫu nhiên, sau đó hash
    // để lưu vào database, còn token gốc sẽ trả về client ở http only cookie để sử
    // dụng cho các lần refresh sau này.
    @Override
    public RefreshToken issueRefreshToken() {
        byte[] tokenBytes = new byte[REFRESH_TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);

        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        Instant expiresAt = Instant.now().plus(authSecurityProperties.refreshTokenTtl());

        return new RefreshToken(rawToken, hashRefreshToken(rawToken), expiresAt);
    }

    // Hash một refresh token để lưu vào database.
    @Override
    public String hashRefreshToken(String rawRefreshToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available for refresh token hashing.", exception);
        }
    }

    // Parse và validate một access token, trả về thông tin chủ thể nếu token hợp
    // lệ, hoặc ném exception nếu token không hợp lệ hoặc đã hết hạn.
    @Override
    public TokenSubject parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();

            Long userId = claims.get("userId", Long.class);
            String sessionFamilyId = claims.get("sessionFamilyId", String.class);

            if (userId == null) {
                throw new JwtAuthenticationException("Access token does not contain a valid user identifier.");
            }

            if (sessionFamilyId == null || sessionFamilyId.isBlank()) {
                throw new JwtAuthenticationException("Access token does not contain a valid session identifier.");
            }

            return new TokenSubject(userId, claims.getSubject(), UUID.fromString(sessionFamilyId));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new JwtAuthenticationException("Invalid or expired access token.", exception);
        }
    }
}
