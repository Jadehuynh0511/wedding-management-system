package com.uit.weddingmanagement.modules.auth.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PermissionResponse", description = "Permission catalog entry exposed for RBAC administration.")
public record PermissionResponse(
        @Schema(description = "Database identifier of the permission.", example = "1") Long id,
        @Schema(description = "Stable business key used for runtime authorization.", example = "USER_GROUP_MANAGE")
                String code,
        @Schema(description = "Vietnamese display name from the locked QD11 specification.", example = "Quan ly nhom nguoi dung")
                String name,
        @Schema(description = "Backend module grouping key.", example = "SYSTEM") String moduleKey,
        @Schema(description = "Functional area metadata for UI grouping.", example = "HE_THONG")
                String functionalGroup,
        @Schema(description = "Human-readable description of the permission.", example = "Quan ly nhom nguoi dung va co cau phan quyen.")
                String description) {}
