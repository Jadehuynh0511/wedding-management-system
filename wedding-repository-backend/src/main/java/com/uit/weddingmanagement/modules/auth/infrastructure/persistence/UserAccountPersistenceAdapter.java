package com.uit.weddingmanagement.modules.auth.infrastructure.persistence;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Component;

import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.Permission;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;
import com.uit.weddingmanagement.modules.auth.domain.model.UserGroup;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.GroupPermissionJpaEntity;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.PermissionJpaEntity;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.UserGroupJpaEntity;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.repository.UserJpaRepository;

@Component
public class UserAccountPersistenceAdapter implements UserAccountQueryPort, UserAccountCommandPort {

    private final UserJpaRepository userJpaRepository;
    private final EntityManager entityManager;

    public UserAccountPersistenceAdapter(UserJpaRepository userJpaRepository, EntityManager entityManager) {
        this.userJpaRepository = userJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<UserAccount> findAllUsers() {
        return userJpaRepository.findAllWithUserGroupOrderByIdAsc().stream()
                .map(this::toManagementDomain)
                .toList();
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return userJpaRepository.findByUsernameWithPermissions(username).map(this::toAuthenticatedDomain);
    }

    @Override
    public Optional<UserAccount> findById(Long userId) {
        return userJpaRepository.findByIdWithPermissions(userId).map(this::toAuthenticatedDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userJpaRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public boolean existsByUsernameAndIdNot(String username, Long userId) {
        return userJpaRepository.existsByUsernameIgnoreCaseAndIdNot(username, userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long userId) {
        return userJpaRepository.existsByEmailIgnoreCaseAndIdNot(email, userId);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userJpaRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long userId) {
        return userJpaRepository.existsByPhoneNumberAndIdNot(phoneNumber, userId);
    }

    @Override
    public UserAccount saveUserAccount(UserAccount userAccount) {
        UserJpaEntity userJpaEntity = resolveEntityForSave(userAccount);
        userJpaEntity.setUserGroup(entityManager.getReference(UserGroupJpaEntity.class, userAccount.userGroup().id()));
        userJpaEntity.setUsername(userAccount.username());
        userJpaEntity.setPasswordHash(userAccount.passwordHash());
        userJpaEntity.setFullName(userAccount.fullName());
        userJpaEntity.setEmail(userAccount.email());
        userJpaEntity.setPhoneNumber(userAccount.phoneNumber());
        userJpaEntity.setStatus(userAccount.status());

        return toManagementDomain(userJpaRepository.save(userJpaEntity));
    }

    private UserAccount toAuthenticatedDomain(UserJpaEntity userJpaEntity) {
        return new UserAccount(
                userJpaEntity.getId(),
                userJpaEntity.getUsername(),
                userJpaEntity.getPasswordHash(),
                userJpaEntity.getFullName(),
                userJpaEntity.getEmail(),
                userJpaEntity.getPhoneNumber(),
                userJpaEntity.getStatus(),
                toAuthenticatedGroupDomain(userJpaEntity.getUserGroup()));
    }

    private UserAccount toManagementDomain(UserJpaEntity userJpaEntity) {
        return new UserAccount(
                userJpaEntity.getId(),
                userJpaEntity.getUsername(),
                userJpaEntity.getPasswordHash(),
                userJpaEntity.getFullName(),
                userJpaEntity.getEmail(),
                userJpaEntity.getPhoneNumber(),
                userJpaEntity.getStatus(),
                toManagementGroupDomain(userJpaEntity.getUserGroup()));
    }

    private UserGroup toAuthenticatedGroupDomain(UserGroupJpaEntity userGroupJpaEntity) {
        Set<Permission> permissions = userGroupJpaEntity.getGroupPermissions().stream()
                .map(GroupPermissionJpaEntity::getPermission)
                .sorted(Comparator.comparing(PermissionJpaEntity::getPermissionCode))
                .map(this::toPermissionDomain)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new UserGroup(
                userGroupJpaEntity.getId(),
                userGroupJpaEntity.getGroupName(),
                userGroupJpaEntity.isSystemGroup(),
                userGroupJpaEntity.getDescription(),
                permissions);
    }

    private UserGroup toManagementGroupDomain(UserGroupJpaEntity userGroupJpaEntity) {
        return new UserGroup(
                userGroupJpaEntity.getId(),
                userGroupJpaEntity.getGroupName(),
                userGroupJpaEntity.isSystemGroup(),
                userGroupJpaEntity.getDescription(),
                Collections.emptySet());
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

    private UserJpaEntity resolveEntityForSave(UserAccount userAccount) {
        if (userAccount.id() == null) {
            return new UserJpaEntity();
        }

        return userJpaRepository.findById(userAccount.id())
                .orElseThrow(() -> new EntityNotFoundException("User account not found with id: " + userAccount.id()));
    }
}
