CREATE TABLE sanctions
(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    internal_note VARCHAR(2000),
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ,
    status VARCHAR(16) NOT NULL,

    CONSTRAINT ck_sanctions_type
        CHECK (type IN ('BAN', 'NOTIFICATION_SUSPENSION')),
    CONSTRAINT ck_sanctions_status
        CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED')),
    CONSTRAINT ck_sanctions_expiration
        CHECK (expires_at IS NULL OR expires_at > created_at)
);

CREATE INDEX idx_sanctions_user_created
    ON sanctions (user_id, created_at DESC);

CREATE INDEX idx_sanctions_active_expiration
    ON sanctions (expires_at)
    WHERE status = 'ACTIVE' AND expires_at IS NOT NULL;

CREATE UNIQUE INDEX uq_sanctions_active_user_type
    ON sanctions (user_id, type)
    WHERE status = 'ACTIVE';
