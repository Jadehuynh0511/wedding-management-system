CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    family_id UUID NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ NULL,
    revoked_at TIMESTAMPTZ NULL,
    revoked_reason VARCHAR(30) NULL,
    replaced_by_token_hash CHAR(64) NULL,
    issued_ip VARCHAR(64) NULL,
    issued_user_agent VARCHAR(512) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_refresh_tokens_revocation_reason CHECK (
        revoked_reason IS NULL OR revoked_reason IN ('ROTATED', 'LOGGED_OUT', 'REUSE_DETECTED')
    )
);

CREATE INDEX idx_refresh_tokens_user_family ON refresh_tokens (user_id, family_id);
CREATE INDEX idx_refresh_tokens_active_family ON refresh_tokens (user_id, family_id, expires_at) WHERE revoked_at IS NULL;
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
