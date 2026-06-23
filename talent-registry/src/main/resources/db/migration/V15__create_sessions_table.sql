-- =============================================================================
-- V15__create_sessions_table.sql
--
-- Creates the `sessions` table matching the Session JPA entity which
-- extends BaseEntity. This is designed for tracking refresh tokens,
-- session duration, and device information per user.
--
-- Target schema:
--   sessions(
--     id                 UUID PRIMARY KEY,
--     user_id            UUID NOT NULL REFERENCES users(id),
--     refresh_token_hash VARCHAR(255) NOT NULL,
--     expires_at         TIMESTAMPTZ NOT NULL,
--     revoked_at         TIMESTAMPTZ,
--     ip_address         VARCHAR(45),
--     user_agent         VARCHAR(500),
--     created_at         TIMESTAMPTZ NOT NULL,
--     updated_at         TIMESTAMPTZ NOT NULL,
--     created_by         VARCHAR(255),
--     updated_by         VARCHAR(255),
--     version            BIGINT NOT NULL DEFAULT 0,
--     is_deleted         BOOLEAN NOT NULL DEFAULT FALSE,
--     deleted_at         TIMESTAMPTZ,
--     deleted_by         VARCHAR(255),
--     UNIQUE(refresh_token_hash)
--   )
-- =============================================================================

CREATE TABLE sessions (
    id                 UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id            UUID         NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    expires_at         TIMESTAMPTZ  NOT NULL,
    revoked_at         TIMESTAMPTZ,
    ip_address         VARCHAR(45),
    user_agent         VARCHAR(500),

    -- BaseEntity audit columns
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by         VARCHAR(255),
    updated_by         VARCHAR(255),
    version            BIGINT       NOT NULL DEFAULT 0,
    is_deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at         TIMESTAMPTZ,
    deleted_by         VARCHAR(255),

    CONSTRAINT pk_sessions                PRIMARY KEY (id),
    CONSTRAINT uq_sessions_token          UNIQUE (refresh_token_hash),
    CONSTRAINT fk_sessions_user           FOREIGN KEY (user_id)
                                              REFERENCES users (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- Performance indexes
-- ---------------------------------------------------------------------------
CREATE INDEX idx_sessions_user_id_active
    ON sessions (user_id)
    WHERE is_deleted = FALSE AND revoked_at IS NULL;

CREATE INDEX idx_sessions_expires_at
    ON sessions (expires_at)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_sessions_not_deleted
    ON sessions (is_deleted, created_at DESC);

-- ---------------------------------------------------------------------------
-- Auto-update trigger for updated_at
-- ---------------------------------------------------------------------------
CREATE TRIGGER trg_sessions_updated_at
    BEFORE UPDATE ON sessions
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
