package com.uit.weddingmanagement.modules.auth.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.RefreshTokenSessionQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenRevocationReason;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenSession;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.repository.RefreshTokenJpaRepository;

// Adapter để kết nối giữa domain và JPA repository, thực hiện các thao tác truy vấn và cập nhật dữ liệu
// liên quan đến refresh token sessions.
@Component
public class RefreshTokenPersistenceAdapter implements RefreshTokenSessionQueryPort, RefreshTokenSessionCommandPort {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final EntityManager entityManager;

    public RefreshTokenPersistenceAdapter(
            RefreshTokenJpaRepository refreshTokenJpaRepository,
            EntityManager entityManager) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
        this.entityManager = entityManager;
    }

    // Tìm một refresh token session theo hash của token, sử dụng lock để đảm bảo an
    // toàn khi thực hiện refresh token.
    @Override
    public Optional<RefreshTokenSession> findByTokenHashForUpdate(String tokenHash) {
        return refreshTokenJpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    // Kiểm tra xem có tồn tại refresh token nào còn hoạt động (chưa bị revoke và
    // chưa hết hạn) trong cùng một familyId hay không, dùng để quyết định có thể
    // rotate token hay không.
    @Override
    public boolean existsActiveTokenInFamily(Long userId, UUID familyId, Instant asOf) {
        return refreshTokenJpaRepository.existsByUser_IdAndFamilyIdAndRevokedAtIsNullAndExpiresAtAfter(
                userId,
                familyId,
                asOf);
    }

    // Lưu một refresh token session mới hoặc cập nhật một session đã tồn tại (ví
    // dụ: khi rotate token).
    @Override
    public void save(RefreshTokenSession refreshTokenSession) {
        RefreshTokenJpaEntity entity = refreshTokenSession.id() == null
                ? new RefreshTokenJpaEntity(
                        entityManager.getReference(UserJpaEntity.class, refreshTokenSession.userId()),
                        refreshTokenSession.familyId(),
                        refreshTokenSession.tokenHash(),
                        refreshTokenSession.expiresAt(),
                        refreshTokenSession.lastUsedAt(),
                        refreshTokenSession.revokedAt(),
                        refreshTokenSession.revokedReason(),
                        refreshTokenSession.replacedByTokenHash(),
                        refreshTokenSession.issuedIp(),
                        refreshTokenSession.issuedUserAgent())
                : entityManager.getReference(RefreshTokenJpaEntity.class, refreshTokenSession.id());

        if (refreshTokenSession.id() != null) {
            entity.setFamilyId(refreshTokenSession.familyId());
            entity.setTokenHash(refreshTokenSession.tokenHash());
            entity.setExpiresAt(refreshTokenSession.expiresAt());
            entity.setLastUsedAt(refreshTokenSession.lastUsedAt());
            entity.setRevokedAt(refreshTokenSession.revokedAt());
            entity.setRevokedReason(refreshTokenSession.revokedReason());
            entity.setReplacedByTokenHash(refreshTokenSession.replacedByTokenHash());
            entity.setIssuedIp(refreshTokenSession.issuedIp());
            entity.setIssuedUserAgent(refreshTokenSession.issuedUserAgent());
        }

        refreshTokenJpaRepository.save(entity);
    }

    // Revoke tất cả các refresh token còn hoạt động trong cùng một familyId.
    @Override
    public void revokeActiveTokensByFamily(
            Long userId,
            UUID familyId,
            Instant revokedAt,
            RefreshTokenRevocationReason reason) {
        refreshTokenJpaRepository.revokeActiveTokensByFamily(userId, familyId, revokedAt, reason);
    }

    // Phương thức tiện ích để chuyển đổi từ entity sang domain model.
    private RefreshTokenSession toDomain(RefreshTokenJpaEntity refreshTokenJpaEntity) {
        return new RefreshTokenSession(
                refreshTokenJpaEntity.getId(),
                refreshTokenJpaEntity.getUser().getId(),
                refreshTokenJpaEntity.getFamilyId(),
                refreshTokenJpaEntity.getTokenHash(),
                refreshTokenJpaEntity.getExpiresAt(),
                refreshTokenJpaEntity.getLastUsedAt(),
                refreshTokenJpaEntity.getRevokedAt(),
                refreshTokenJpaEntity.getRevokedReason(),
                refreshTokenJpaEntity.getReplacedByTokenHash(),
                refreshTokenJpaEntity.getIssuedIp(),
                refreshTokenJpaEntity.getIssuedUserAgent());
    }
}
