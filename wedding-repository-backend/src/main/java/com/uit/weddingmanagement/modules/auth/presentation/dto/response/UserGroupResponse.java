package com.uit.weddingmanagement.modules.auth.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserGroupResponse", description = "RBAC group information exposed to the administration UI.")
public record UserGroupResponse(
        @Schema(description = "Database identifier of the group.", example = "1") Long id,
        @Schema(description = "Stable group name.", example = "ADMIN") String name,
        @Schema(description = "Indicates whether the group is a system-managed group.", example = "true")
                boolean systemGroup,
        @Schema(description = "Vietnamese description for the group.", example = "Nhom quan tri he thong co toan quyen RBAC.")
                String description) {}
