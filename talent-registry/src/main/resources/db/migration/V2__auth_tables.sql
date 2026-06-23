-- =============================================================================
-- V2__auth_tables.sql
-- Authentication & session management tables
-- Covers: refresh_tokens, password_reset_tokens, email_verification_tokens,
--         oauth2_linked_accounts, user_sessions
-- =============================================================================

-- ---------------------------------------------------------------------------
-- REFRESH TOKENS  (JWT refresh token store for rotation & revocation)
-- ---------------------------------------------------------------------------

CREATE TABLE refresh_tokens (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,            -- SHA-256 hash of the raw token
    device_info VARCHAR(500),
    ip_address  VARCHAR(45),
    issued_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked_at  TIMESTAMPTZ,
    replaced_by UUID,                             -- points to the next token after rotation
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user      ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires   ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_tokens_revoked   ON refresh_tokens (revoked_at) WHERE revoked_at IS NULL;

-- ---------------------------------------------------------------------------
-- PASSWORD RESET TOKENS
-- ---------------------------------------------------------------------------

CREATE TABLE password_reset_tokens (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,
    issued_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ  NOT NULL,
    used_at     TIMESTAMPTZ,
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT uq_prt_hash UNIQUE (token_hash),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_prt_user    ON password_reset_tokens (user_id);
CREATE INDEX idx_prt_expires ON password_reset_tokens (expires_at);

-- ---------------------------------------------------------------------------
-- EMAIL VERIFICATION TOKENS
-- ---------------------------------------------------------------------------

CREATE TABLE email_verification_tokens (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,
    new_email   VARCHAR(255),                     -- populated when user changes email
    issued_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ  NOT NULL,
    verified_at TIMESTAMPTZ,
    CONSTRAINT pk_email_verification_tokens PRIMARY KEY (id),
    CONSTRAINT uq_evt_hash UNIQUE (token_hash),
    CONSTRAINT fk_evt_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_evt_user    ON email_verification_tokens (user_id);
CREATE INDEX idx_evt_expires ON email_verification_tokens (expires_at);

-- ---------------------------------------------------------------------------
-- OAUTH2 LINKED ACCOUNTS  (Google, GitHub, LinkedIn, etc.)
-- ---------------------------------------------------------------------------

CREATE TABLE oauth2_linked_accounts (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL,
    provider        VARCHAR(50)  NOT NULL,         -- e.g. 'google', 'github', 'linkedin'
    provider_user_id VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    access_token    TEXT,
    refresh_token   TEXT,
    token_expires_at TIMESTAMPTZ,
    linked_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_oauth2_linked_accounts PRIMARY KEY (id),
    CONSTRAINT uq_oauth2_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT fk_ola_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_ola_user     ON oauth2_linked_accounts (user_id);
CREATE INDEX idx_ola_provider ON oauth2_linked_accounts (provider, provider_user_id);

-- ---------------------------------------------------------------------------
-- USER SESSIONS  (optional server-side session tracking)
-- ---------------------------------------------------------------------------

CREATE TABLE user_sessions (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL,
    session_token_hash VARCHAR(255) NOT NULL,
    ip_address   VARCHAR(45),
    user_agent   VARCHAR(500),
    device_type  VARCHAR(50),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_seen_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at   TIMESTAMPTZ  NOT NULL,
    terminated_at TIMESTAMPTZ,
    CONSTRAINT pk_user_sessions PRIMARY KEY (id),
    CONSTRAINT uq_user_sessions_token UNIQUE (session_token_hash),
    CONSTRAINT fk_us_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_sessions_user    ON user_sessions (user_id);
CREATE INDEX idx_user_sessions_expires ON user_sessions (expires_at);
CREATE INDEX idx_user_sessions_active  ON user_sessions (terminated_at) WHERE terminated_at IS NULL;

-- ---------------------------------------------------------------------------
-- AUDIT: track failed login attempts for rate-limiting / lockout
-- ---------------------------------------------------------------------------

CREATE TABLE login_attempts (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    email        VARCHAR(255) NOT NULL,
    ip_address   VARCHAR(45),
    succeeded    BOOLEAN     NOT NULL DEFAULT FALSE,
    attempted_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_login_attempts PRIMARY KEY (id)
);

CREATE INDEX idx_login_attempts_email ON login_attempts (email, attempted_at DESC);
CREATE INDEX idx_login_attempts_ip    ON login_attempts (ip_address, attempted_at DESC);
