package com.uit.weddingmanagement.modules.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "RefreshTokenRequest", description = "Refresh token payload used to rotate the session.")
public record RefreshTokenRequest(
        @Schema(description = "Opaque refresh token that was issued during login or the previous refresh.", example = "M8oFtvxXw4Tr5p7a...")
        @NotBlank(message = "Refresh token is required.")
        String refreshToken) {
}
