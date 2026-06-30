package com.uit.weddingmanagement.modules.auth.presentation.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

// Dto để map với LoginResult từ use case và trả về response cho client.
@Schema(name = "LoginResponse", description = "Access token and rotated refresh token returned after a successful login.")
public record LoginResponse(
                @Schema(description = "Signed JWT access token.", example = "eyJhbGciOiJIUzI1NiJ9...") String accessToken,
                @Schema(description = "Token type to send in the Authorization header.", example = "Bearer") String tokenType,
                @Schema(description = "UTC timestamp when the access token expires.", example = "2026-05-25T10:15:30Z") Instant expiresAt,
                @Schema(description = "Opaque refresh token. Store it securely and rotate it on every refresh.", example = "M8oFtvxXw4Tr5p7a...") String refreshToken,
                @Schema(description = "UTC timestamp when the refresh token expires.", example = "2026-06-24T10:15:30Z") Instant refreshExpiresAt) {
}
