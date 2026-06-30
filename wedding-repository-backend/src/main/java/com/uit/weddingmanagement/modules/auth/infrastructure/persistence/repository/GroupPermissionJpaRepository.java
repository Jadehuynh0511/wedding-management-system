package com.uit.weddingmanagement.modules.auth.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.GroupPermissionId;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.GroupPermissionJpaEntity;

public interface GroupPermissionJpaRepository extends JpaRepository<GroupPermissionJpaEntity, GroupPermissionId> {

    @Query(
            """
            select permission.permissionCode
            from GroupPermissionJpaEntity groupPermission
            join groupPermission.permission permission
            where groupPermission.userGroup.id = :groupId
            order by permission.id asc
            """)
    List<String> findPermissionCodesByGroupId(@Param("groupId") Long groupId);
}
