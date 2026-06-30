package com.uit.weddingmanagement.modules.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Credentials used to obtain a JWT access token.")
public record LoginRequest(
        @Schema(description = "Application username.", example = "admin") @NotBlank(message = "Username is required.") String username,
        @Schema(description = "Raw password for the supplied username.", example = "admin123!") @NotBlank(message = "Password is required.") String password) {
}
