package com.uit.weddingmanagement.modules.auth.presentation.dto.response;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "GroupPermissionResponse",
    description = "Current permission codes assigned to a specific RBAC group.")
public record GroupPermissionResponse(
        @Schema(description = "Database identifier of the group.", example = "2") Long groupId,
        @Schema(description = "Effective permission codes currently assigned to the group.", example = "[\"WEDDING_BOOKING_VIEW\"]")
                Set<String> permissionCodes) {}
