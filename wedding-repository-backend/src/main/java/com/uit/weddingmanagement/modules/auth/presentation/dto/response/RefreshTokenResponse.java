package com.uit.weddingmanagement.modules.auth.presentation.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

// Dto để map với RefreshTokenResult từ use case và trả về response.
// Các annotations này là để generate ra schema cho swagger UI.
@Schema(name = "RefreshTokenResponse", description = "Rotated access token and refresh token returned after a successful refresh.")
public record RefreshTokenResponse(
                @Schema(description = "Signed JWT access token.", example = "eyJhbGciOiJIUzI1NiJ9...") String accessToken,
                @Schema(description = "Token type to send in the Authorization header.", example = "Bearer") String tokenType,
                @Schema(description = "UTC timestamp when the access token expires.", example = "2026-05-25T10:30:30Z") Instant expiresAt,
                @Schema(description = "Next opaque refresh token that replaces the previous one.", example = "ndY9P0Lr0m8SP3X2...") String refreshToken,
                @Schema(description = "UTC timestamp when the refresh token expires.", example = "2026-06-24T10:30:30Z") Instant refreshExpiresAt) {
}
