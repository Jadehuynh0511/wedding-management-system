-- V10: Create audit_logs table for system activity tracking (QD13)
-- This table is immutable: only INSERT and SELECT operations are allowed.

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_user_id BIGINT NULL,
    actor_username VARCHAR(100) NOT NULL,
    actor_group_name VARCHAR(100) NOT NULL,
    action_code VARCHAR(50) NOT NULL,
    module_key VARCHAR(50) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id VARCHAR(100) NULL,
    target_label VARCHAR(255) NULL,
    result_status VARCHAR(10) NOT NULL,
    description TEXT NOT NULL,
    error_message TEXT NULL,
    details_json JSONB NULL,
    CONSTRAINT chk_audit_result_status CHECK (result_status IN ('SUCCESS', 'FAIL'))
);

-- Indexes for performance based on expected search patterns
CREATE INDEX idx_audit_logs_occurred_at ON audit_logs (occurred_at DESC);
CREATE INDEX idx_audit_logs_actor_username ON audit_logs (actor_username, occurred_at DESC);
CREATE INDEX idx_audit_logs_action_code ON audit_logs (action_code, occurred_at DESC);
CREATE INDEX idx_audit_logs_result_status ON audit_logs (result_status, occurred_at DESC);
CREATE INDEX idx_audit_logs_module_key ON audit_logs (module_key, occurred_at DESC);

-- Note: No UPDATE or DELETE triggers/constraints are added here via Flyway, 
-- but the application layer must ensure immutability by not providing such APIs.
-- We also do not use foreign keys to users/groups to ensure logs persist even if the actor is deleted.
