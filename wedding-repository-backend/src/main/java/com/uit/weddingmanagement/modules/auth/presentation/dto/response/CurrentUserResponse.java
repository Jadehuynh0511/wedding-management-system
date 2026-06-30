package com.uit.weddingmanagement.modules.auth.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

// Dto này để map với CurrentUserResult từ use case và trả về response.
@Schema(name = "CurrentUserResponse", description = "Authenticated user profile resolved from the bearer token.")
public record CurrentUserResponse(
        @Schema(description = "Database identifier of the current user.", example = "1") Long id,
        @Schema(description = "Username of the authenticated user.", example = "admin") String username,
        @Schema(description = "RBAC group assigned to the user.", example = "ADMIN") String groupName,
        @Schema(description = "Effective permission codes loaded for the current user.", example = "[\"SYSTEM_USER_VIEW\",\"SYSTEM_ROLE_ASSIGN\"]") Set<String> permissionCodes) {
}
