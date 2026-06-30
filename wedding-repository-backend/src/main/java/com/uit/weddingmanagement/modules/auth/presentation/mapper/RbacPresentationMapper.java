package com.uit.weddingmanagement.modules.auth.presentation.mapper;

import com.uit.weddingmanagement.modules.auth.application.model.result.PermissionCatalogResult;
import com.uit.weddingmanagement.modules.auth.application.model.result.UserGroupResult;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.PermissionResponse;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.UserGroupResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi application-layer result sang presentation-layer response
 * cho các API RBAC administration (permissions, groups, assignments).
 */
@Component
public class RbacPresentationMapper {

    public PermissionResponse toPermissionResponse(PermissionCatalogResult permissionCatalogResult) {
        return new PermissionResponse(
                permissionCatalogResult.id(),
                permissionCatalogResult.code(),
                permissionCatalogResult.name(),
                permissionCatalogResult.moduleKey(),
                permissionCatalogResult.functionalGroup(),
                permissionCatalogResult.description());
    }

    public UserGroupResponse toUserGroupResponse(UserGroupResult userGroupResult) {
        return new UserGroupResponse(
                userGroupResult.id(),
                userGroupResult.name(),
                userGroupResult.systemGroup(),
                userGroupResult.description());
    }
}
