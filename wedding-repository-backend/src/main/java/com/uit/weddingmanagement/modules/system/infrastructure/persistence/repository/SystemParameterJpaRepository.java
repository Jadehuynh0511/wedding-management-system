package com.uit.weddingmanagement.modules.system.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.system.infrastructure.persistence.entity.SystemParameterJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemParameterJpaRepository extends JpaRepository<SystemParameterJpaEntity, Short> {}
