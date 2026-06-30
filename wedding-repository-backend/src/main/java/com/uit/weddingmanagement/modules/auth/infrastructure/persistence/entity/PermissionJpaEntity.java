package com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.uit.weddingmanagement.common.entity.BaseEntity;

// Entity được ánh xạ từ bảng permissions trong database.
@Entity
@Table(name = "permissions")
public class PermissionJpaEntity extends BaseEntity {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_code", nullable = false, unique = true, length = 100)
    private String permissionCode;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "module_key", nullable = false, length = 100)
    private String moduleKey;

    @Column(name = "functional_group", nullable = false, length = 20)
    private String functionalGroup;

    @Column(name = "description")
    private String description;

    public Long getId() {
        return id;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public String getFunctionalGroup() {
        return functionalGroup;
    }

    public String getDescription() {
        return description;
    }
}
