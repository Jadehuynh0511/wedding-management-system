package com.uit.weddingmanagement.modules.auth.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenRevocationReason;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;

// Repository JPA để quản lý RefreshTokenSession, bao gồm các truy vấn cần thiết
// cho việc refresh token và revoke token theo familyId.
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {

        // Truy vấn để tìm một refresh token theo hash của nó, sử dụng lock để đảm bảo
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

        // Kiểm tra xem có tồn tại refresh token nào còn hoạt động (chưa bị revoke và
        // chưa hết hạn) trong cùng một familyId hay không, dùng để quyết định có thể
        // rotate token hay không.
        boolean existsByUser_IdAndFamilyIdAndRevokedAtIsNullAndExpiresAtAfter(Long userId, UUID familyId, Instant asOf);

        // Query để revoke tất cả các refresh token còn hoạt động trong cùng một
        // familyId (ví dụ: khi người dùng đăng xuất hoặc khi phát hiện tái sử dụng),
        // cập nhật thời điểm bị revoke và lý do revoke.
        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("""
                        update RefreshTokenJpaEntity refreshToken
                           set refreshToken.revokedAt = :revokedAt,
                               refreshToken.revokedReason = :reason,
                               refreshToken.updatedAt = :revokedAt
                         where refreshToken.user.id = :userId
                           and refreshToken.familyId = :familyId
                           and refreshToken.revokedAt is null
                        """)
        int revokeActiveTokensByFamily(
                        @Param("userId") Long userId,
                        @Param("familyId") UUID familyId,
                        @Param("revokedAt") Instant revokedAt,
                        @Param("reason") RefreshTokenRevocationReason reason);
}
