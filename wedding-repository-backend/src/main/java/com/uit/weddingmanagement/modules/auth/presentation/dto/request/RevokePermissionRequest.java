package com.uit.weddingmanagement.modules.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(name = "RevokePermissionRequest", description = "Payload used by ADMIN to revoke one permission from a group.")
public record RevokePermissionRequest(
        @Schema(description = "Target RBAC group id.", example = "2")
                @NotNull(message = "Group id is required.")
                @Positive(message = "Group id must be greater than 0.")
                Long groupId,
        @Schema(description = "Permission code to revoke.", example = "WEDDING_BOOKING_VIEW")
                @NotBlank(message = "Permission code is required.")
                String permissionCode) {}
