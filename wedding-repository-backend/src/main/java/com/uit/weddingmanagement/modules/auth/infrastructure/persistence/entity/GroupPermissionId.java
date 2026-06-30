package com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Id của bảng nối group_permissions
// Dùng để đánh dấu mỗi group có permission nào
@Embeddable
public class GroupPermissionId implements Serializable {

    @Column(name = "user_group_id", nullable = false)
    private Long userGroupId;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    public GroupPermissionId() {
    }

    public GroupPermissionId(Long userGroupId, Long permissionId) {
        this.userGroupId = userGroupId;
        this.permissionId = permissionId;
    }

    public Long getUserGroupId() {
        return userGroupId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof GroupPermissionId that)) {
            return false;
        }
        return Objects.equals(userGroupId, that.userGroupId) && Objects.equals(permissionId, that.permissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userGroupId, permissionId);
    }
}
