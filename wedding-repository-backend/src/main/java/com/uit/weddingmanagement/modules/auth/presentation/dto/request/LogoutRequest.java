package com.uit.weddingmanagement.modules.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LogoutRequest", description = "Refresh token payload used to revoke the current session family.")
public record LogoutRequest(
        @Schema(description = "Opaque refresh token representing the session family that should be revoked.", example = "ndY9P0Lr0m8SP3X2...")
        @NotBlank(message = "Refresh token is required.")
        String refreshToken) {
}
