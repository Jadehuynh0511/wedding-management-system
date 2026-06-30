package com.uit.weddingmanagement.modules.auth.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.PermissionJpaEntity;

public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, Long> {

    List<PermissionJpaEntity> findAllByOrderByIdAsc();

    Optional<PermissionJpaEntity> findByPermissionCode(String permissionCode);
}
