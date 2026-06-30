package com.uit.weddingmanagement.modules.auth.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.UserGroupJpaEntity;

public interface UserGroupJpaRepository extends JpaRepository<UserGroupJpaEntity, Long> {

    List<UserGroupJpaEntity> findAllByOrderByIdAsc();
}
