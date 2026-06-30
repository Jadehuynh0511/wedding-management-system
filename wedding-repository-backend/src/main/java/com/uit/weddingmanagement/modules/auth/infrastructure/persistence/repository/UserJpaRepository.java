package com.uit.weddingmanagement.modules.auth.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.UserJpaEntity;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    @Query("""
                    select userEntity
                    from UserJpaEntity userEntity
                    join fetch userEntity.userGroup userGroup
                    order by userEntity.id asc
                    """)
    List<UserJpaEntity> findAllWithUserGroupOrderByIdAsc();

    @Query("""
                    select distinct userEntity
                    from UserJpaEntity userEntity
                    join fetch userEntity.userGroup userGroup
                    left join fetch userGroup.groupPermissions groupPermission
                    left join fetch groupPermission.permission permission
                    where lower(userEntity.username) = lower(:username)
                    """)
    Optional<UserJpaEntity> findByUsernameWithPermissions(String username);

    @Query("""
                    select distinct userEntity
                    from UserJpaEntity userEntity
                    join fetch userEntity.userGroup userGroup
                    left join fetch userGroup.groupPermissions groupPermission
                    left join fetch groupPermission.permission permission
                    where userEntity.id = :userId
                    """)
    Optional<UserJpaEntity> findByIdWithPermissions(Long userId);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long userId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long userId);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long userId);
}
