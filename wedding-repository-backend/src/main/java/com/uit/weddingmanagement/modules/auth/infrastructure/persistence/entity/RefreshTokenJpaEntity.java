package com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.uit.weddingmanagement.common.entity.BaseEntity;
import com.uit.weddingmanagement.modules.auth.domain.model.RefreshTokenRevocationReason;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ nhiều-nhất với UserJpaEntity (một user có thể có nhiều refresh
    // token).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    // familyId dùng để nhóm các refresh token liên quan đến cùng một session (ví
    // dụ: cùng một lần đăng nhập).
    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    // Lưu hash của refresh token để tăng cường bảo mật (thay vì lưu token gốc).
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // Lưu thông tin lần cuối cùng token được sử dụng để phát hiện các trường hợp
    // refresh token bị lạm dụng (ví dụ: token bị đánh cắp và sử dụng ở nơi khác).
    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    // Nếu token bị revoke, lưu thời điểm bị revoke và lý do để phân biệt giữa các
    // trường hợp như rotate, logout, hoặc reuse detection.
    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revoked_reason", length = 30)
    private RefreshTokenRevocationReason revokedReason;

    // Nếu token bị rotate, lưu hash của refresh token mới thay thế để có thể theo
    // dõi chuỗi rotate.
    @Column(name = "replaced_by_token_hash", length = 64)
    private String replacedByTokenHash;

    // Lưu thông tin IP và user agent khi phát hành token để hỗ trợ phân tích và
    // phát hiện các hoạt động đáng ngờ.
    @Column(name = "issued_ip", length = 64)
    private String issuedIp;

    // Lưu user agent để hỗ trợ phân tích và phát hiện các hoạt động đáng ngờ (ví
    // dụ: token được sử dụng từ một trình duyệt hoặc thiết bị khác với lần đăng
    // nhập ban đầu).
    @Column(name = "issued_user_agent", length = 512)
    private String issuedUserAgent;

    protected RefreshTokenJpaEntity() {
    }

    public RefreshTokenJpaEntity(
            UserJpaEntity user,
            UUID familyId,
            String tokenHash,
            Instant expiresAt,
            Instant lastUsedAt,
            Instant revokedAt,
            RefreshTokenRevocationReason revokedReason,
            String replacedByTokenHash,
            String issuedIp,
            String issuedUserAgent) {
        this.user = user;
        this.familyId = familyId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.lastUsedAt = lastUsedAt;
        this.revokedAt = revokedAt;
        this.revokedReason = revokedReason;
        this.replacedByTokenHash = replacedByTokenHash;
        this.issuedIp = issuedIp;
        this.issuedUserAgent = issuedUserAgent;
    }

    public Long getId() {
        return id;
    }

    public UserJpaEntity getUser() {
        return user;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public void setFamilyId(UUID familyId) {
        this.familyId = familyId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public RefreshTokenRevocationReason getRevokedReason() {
        return revokedReason;
    }

    public void setRevokedReason(RefreshTokenRevocationReason revokedReason) {
        this.revokedReason = revokedReason;
    }

    public String getReplacedByTokenHash() {
        return replacedByTokenHash;
    }

    public void setReplacedByTokenHash(String replacedByTokenHash) {
        this.replacedByTokenHash = replacedByTokenHash;
    }

    public String getIssuedIp() {
        return issuedIp;
    }

    public void setIssuedIp(String issuedIp) {
        this.issuedIp = issuedIp;
    }

    public String getIssuedUserAgent() {
        return issuedUserAgent;
    }

    public void setIssuedUserAgent(String issuedUserAgent) {
        this.issuedUserAgent = issuedUserAgent;
    }
}
