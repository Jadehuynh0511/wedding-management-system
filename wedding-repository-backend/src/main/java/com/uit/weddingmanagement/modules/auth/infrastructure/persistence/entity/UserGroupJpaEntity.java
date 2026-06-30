package com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.uit.weddingmanagement.common.entity.BaseEntity;

// Group entity giữ tập liên kết đến bảng group_permissions.
// Quyền thật nằm ở permission rows, group chỉ là container.
@Entity
@Table(name = "user_groups")
public class UserGroupJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name", nullable = false, unique = true, length = 100)
    private String groupName;

    @Column(name = "description")
    private String description;

    @Column(name = "system_group", nullable = false)
    private boolean systemGroup;

    @OneToMany(mappedBy = "userGroup", fetch = FetchType.LAZY)
    private Set<GroupPermissionJpaEntity> groupPermissions = new LinkedHashSet<>();

    // Getters
    public Long getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSystemGroup() {
        return systemGroup;
    }

    public Set<GroupPermissionJpaEntity> getGroupPermissions() {
        return groupPermissions;
    }
}
