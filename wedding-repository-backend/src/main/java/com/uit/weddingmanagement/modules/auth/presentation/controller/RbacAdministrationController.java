package com.uit.weddingmanagement.modules.auth.presentation.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.modules.auth.application.model.command.AssignPermissionCommand;
import com.uit.weddingmanagement.modules.auth.application.model.command.RevokePermissionCommand;
import com.uit.weddingmanagement.modules.auth.application.port.in.AssignPermissionToGroupUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.in.GetGroupPermissionsUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.in.ListPermissionCatalogUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.in.ListUserGroupsUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.in.RevokePermissionFromGroupUseCase;
import com.uit.weddingmanagement.modules.auth.presentation.dto.request.AssignPermissionRequest;
import com.uit.weddingmanagement.modules.auth.presentation.dto.request.RevokePermissionRequest;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.GroupPermissionResponse;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.PermissionResponse;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.UserGroupResponse;
import com.uit.weddingmanagement.modules.auth.presentation.mapper.RbacPresentationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

// Controller này chỉ phụ trách contract HTTP cho RBAC administration APIs.
// Tat ca rule nghiep vu nhay cam van nam o use case de co the tai su dung ngoai web layer.
@Validated
@RestController
@RequestMapping("/api")
@Tag(name = "RBAC Administration", description = "APIs for permission catalog, user groups, and group-permission administration.")
public class RbacAdministrationController {

    private final ListPermissionCatalogUseCase listPermissionCatalogUseCase;
    private final ListUserGroupsUseCase listUserGroupsUseCase;
    private final GetGroupPermissionsUseCase getGroupPermissionsUseCase;
    private final AssignPermissionToGroupUseCase assignPermissionToGroupUseCase;
    private final RevokePermissionFromGroupUseCase revokePermissionFromGroupUseCase;
    private final RbacPresentationMapper rbacPresentationMapper;

    // Constructor injection được sử dụng để đảm bảo tất cả dependencies được cung cấp
    public RbacAdministrationController(
            ListPermissionCatalogUseCase listPermissionCatalogUseCase,
            ListUserGroupsUseCase listUserGroupsUseCase,
            GetGroupPermissionsUseCase getGroupPermissionsUseCase,
            AssignPermissionToGroupUseCase assignPermissionToGroupUseCase,
            RevokePermissionFromGroupUseCase revokePermissionFromGroupUseCase,
            RbacPresentationMapper rbacPresentationMapper) {
        this.listPermissionCatalogUseCase = listPermissionCatalogUseCase;
        this.listUserGroupsUseCase = listUserGroupsUseCase;
        this.getGroupPermissionsUseCase = getGroupPermissionsUseCase;
        this.assignPermissionToGroupUseCase = assignPermissionToGroupUseCase;
        this.revokePermissionFromGroupUseCase = revokePermissionFromGroupUseCase;
        this.rbacPresentationMapper = rbacPresentationMapper;
    }

    /**
     * Các endpoint trong controller này đều yêu cầu authentication (được đảm bảo
     * bởi security configuration) và authorization (được đảm bảo bởi @PreAuthorize
     * hoặc logic trong use case)
     */

    // Lấy danh sách permission catalog của RBAC system
    @GetMapping("/permissions")
    @Operation(summary = "List the RBAC permission catalog", description = "Returns the locked QD11 permission catalog with metadata for admin UI or Postman.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permission catalog loaded successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, or expired.")
    })
    public ApiResponse<List<PermissionResponse>> listPermissions() {
        List<PermissionResponse> permissions = listPermissionCatalogUseCase.listPermissionCatalog().stream()
                .map(rbacPresentationMapper::toPermissionResponse)
                .toList();

        return ApiResponse.success("Permission catalog loaded successfully.", permissions);
    }

    // Lấy danh sách user group của RBAC system
    @GetMapping("/groups")
    @Operation(summary = "List RBAC user groups", description = "Returns all groups available for permission administration.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User groups loaded successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, or expired.")
    })
    public ApiResponse<List<UserGroupResponse>> listUserGroups() {
        List<UserGroupResponse> groups = listUserGroupsUseCase.listUserGroups().stream()
                .map(rbacPresentationMapper::toUserGroupResponse)
                .toList();

        return ApiResponse.success("User groups loaded successfully.", groups);
    }

    // Lấy danh sách permission code đang được gán cho một group
    @GetMapping("/groups/{groupId}/permissions")
    @Operation(summary = "Get permissions assigned to a group", description = "Returns the effective permission codes currently mapped to the target group.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Group permissions loaded successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, or expired."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Target group does not exist.")
    })
    public ApiResponse<GroupPermissionResponse> getGroupPermissions(
            @PathVariable @Positive(message = "Group id must be greater than 0.") Long groupId) {
        var result = getGroupPermissionsUseCase.getGroupPermissions(groupId);
        return ApiResponse.success(
                "Group permissions loaded successfully.",
                new GroupPermissionResponse(result.groupId(), result.permissionCodes()));
    }

    // Gán một permission cho một group.
    // Chỉ cho phép admin mới được gọi endpoint này. Logic kiểm tra sẽ nằm trong
    // authorizationService để có thể tái sử dụng ở nhiều chỗ khác nếu cần.
    @PostMapping("/permissions/assign")
    @PreAuthorize("@authorizationService.isAdmin()")
    @Operation(summary = "Assign one permission to a group", description = "ADMIN-only endpoint used to add one permission mapping for a target group.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permission assignment processed successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Request payload validation failed."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, or expired."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Current user is not allowed to administer permissions."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Target group or permission does not exist.")
    })
    public ApiResponse<Void> assignPermission(@Valid @RequestBody AssignPermissionRequest request) {
        boolean changed = assignPermissionToGroupUseCase.assignPermissionToGroup(
                new AssignPermissionCommand(request.groupId(), request.permissionCode()));

        if (!changed) {
            return ApiResponse.success("Permission was already assigned to the group.");
        }

        return ApiResponse.success("Permission assigned to group successfully.");
    }

    // Thu hồi một permission khỏi một group.
    // Chỉ cho phép admin mới được gọi endpoint này
    @DeleteMapping("/permissions/revoke")
    @PreAuthorize("@authorizationService.isAdmin()")
    @Operation(summary = "Revoke one permission from a group", description = "ADMIN-only endpoint used to remove one permission mapping from a target group.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permission revoke processed successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Request payload validation failed."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, or expired."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Current user is not allowed to administer permissions."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Target group or permission does not exist.")
    })
    public ApiResponse<Void> revokePermission(@Valid @RequestBody RevokePermissionRequest request) {
        boolean changed = revokePermissionFromGroupUseCase.revokePermissionFromGroup(
                new RevokePermissionCommand(request.groupId(), request.permissionCode()));

        if (!changed) {
            return ApiResponse.success("Permission was already absent from the group.");
        }

        return ApiResponse.success("Permission revoked from group successfully.");
    }
}
