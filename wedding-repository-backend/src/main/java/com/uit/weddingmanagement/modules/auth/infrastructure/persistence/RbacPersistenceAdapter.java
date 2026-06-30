package com.uit.weddingmanagement.modules.auth.infrastructure.persistence;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.uit.weddingmanagement.modules.auth.application.port.out.GroupPermissionCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupPermissionQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.PermissionQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.Permission;
import com.uit.weddingmanagement.modules.auth.domain.model.UserGroup;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.GroupPermissionId;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.GroupPermissionJpaEntity;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.PermissionJpaEntity;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.UserGroupJpaEntity;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.repository.GroupPermissionJpaRepository;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.repository.PermissionJpaRepository;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.repository.UserGroupJpaRepository;

// Adapter riêng cho RBAC catalog va group-permission administration
// Application layer chỉ biết đến ports; mọi chi tiết JPA nằm lại ở đây
@Component
public class RbacPersistenceAdapter
        implements PermissionQueryPort, GroupQueryPort, GroupPermissionQueryPort, GroupPermissionCommandPort {

    private final PermissionJpaRepository permissionJpaRepository;
    private final UserGroupJpaRepository userGroupJpaRepository;
    private final GroupPermissionJpaRepository groupPermissionJpaRepository;
    private final EntityManager entityManager;

    public RbacPersistenceAdapter(
            PermissionJpaRepository permissionJpaRepository,
            UserGroupJpaRepository userGroupJpaRepository,
            GroupPermissionJpaRepository groupPermissionJpaRepository,
            EntityManager entityManager) {
        this.permissionJpaRepository = permissionJpaRepository;
        this.userGroupJpaRepository = userGroupJpaRepository;
        this.groupPermissionJpaRepository = groupPermissionJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<Permission> findAllPermissions() {
        return permissionJpaRepository.findAllByOrderByIdAsc().stream()
                .map(this::toPermissionDomain)
                .toList();
    }

    @Override
    public Optional<Permission> findPermissionByCode(String permissionCode) {
        return permissionJpaRepository.findByPermissionCode(permissionCode).map(this::toPermissionDomain);
    }

    @Override
    public List<UserGroup> findAllGroups() {
        return userGroupJpaRepository.findAllByOrderByIdAsc().stream()
                .map(this::toUserGroupDomainWithoutPermissions)
                .toList();
    }

    @Override
    public Optional<UserGroup> findGroupById(Long groupId) {
        return userGroupJpaRepository.findById(groupId).map(this::toUserGroupDomainWithoutPermissions);
    }

    @Override
    public Set<String> findPermissionCodesByGroupId(Long groupId) {
        return groupPermissionJpaRepository.findPermissionCodesByGroupId(groupId).stream()
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public boolean existsByGroupIdAndPermissionId(Long groupId, Long permissionId) {
        return groupPermissionJpaRepository.existsById(new GroupPermissionId(groupId, permissionId));
    }

    @Override
    public void assignPermissionToGroup(Long groupId, Long permissionId) {
        // getReference giup tao relation moi ma khong can query lai full row khi
        // service da validate ton tai.
        UserGroupJpaEntity userGroupReference = entityManager.getReference(UserGroupJpaEntity.class, groupId);
        PermissionJpaEntity permissionReference = entityManager.getReference(PermissionJpaEntity.class, permissionId);

        groupPermissionJpaRepository.save(new GroupPermissionJpaEntity(userGroupReference, permissionReference));
    }

    @Override
    public void revokePermissionFromGroup(Long groupId, Long permissionId) {
        groupPermissionJpaRepository.deleteById(new GroupPermissionId(groupId, permissionId));
    }

    private Permission toPermissionDomain(PermissionJpaEntity permissionJpaEntity) {
        return new Permission(
                permissionJpaEntity.getId(),
                permissionJpaEntity.getPermissionCode(),
                permissionJpaEntity.getDisplayName(),
                permissionJpaEntity.getModuleKey(),
                permissionJpaEntity.getFunctionalGroup(),
                permissionJpaEntity.getDescription());
    }

    private UserGroup toUserGroupDomainWithoutPermissions(UserGroupJpaEntity userGroupJpaEntity) {
        return new UserGroup(
                userGroupJpaEntity.getId(),
                userGroupJpaEntity.getGroupName(),
                userGroupJpaEntity.isSystemGroup(),
                userGroupJpaEntity.getDescription(),
                Collections.emptySet());
    }
}
