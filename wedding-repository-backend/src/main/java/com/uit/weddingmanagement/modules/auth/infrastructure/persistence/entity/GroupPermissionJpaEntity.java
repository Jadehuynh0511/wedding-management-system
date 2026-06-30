package com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

// Bảng nối group_permissions biểu diễn "group nào có permission nào".
@Entity
@Table(name = "group_permissions")
public class GroupPermissionJpaEntity {

    @EmbeddedId
    private GroupPermissionId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userGroupId")
    @JoinColumn(name = "user_group_id", nullable = false)
    private UserGroupJpaEntity userGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionJpaEntity permission;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected GroupPermissionJpaEntity() {
        // JPA can 1 constructor rong de hydrate entity.
    }

    public GroupPermissionJpaEntity(UserGroupJpaEntity userGroup, PermissionJpaEntity permission) {
        // Adapter tao mapping moi chi can reference den 2 bang goc va composite key tuong ung.
        this.id = new GroupPermissionId(userGroup.getId(), permission.getId());
        this.userGroup = userGroup;
        this.permission = permission;
    }

    public GroupPermissionId getId() {
        return id;
    }

    public UserGroupJpaEntity getUserGroup() {
        return userGroup;
    }

    public PermissionJpaEntity getPermission() {
        return permission;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
