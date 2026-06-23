-- =============================================================================
-- V4__audit_columns.sql
-- Adds BaseEntity audit columns to every domain table created in V1–V3.
-- Columns match com.zencube.registry.common.BaseEntity exactly:
--   created_at  TIMESTAMPTZ NOT NULL
--   updated_at  TIMESTAMPTZ NOT NULL
--   created_by  VARCHAR(255)
--   updated_by  VARCHAR(255)
--   version     BIGINT      NOT NULL DEFAULT 0
--   is_deleted  BOOLEAN     NOT NULL DEFAULT FALSE
--   deleted_at  TIMESTAMPTZ
--   deleted_by  VARCHAR(255)
-- =============================================================================

-- Helper: a DO block runs all ALTER TABLE statements in one transaction.
-- Tables are processed in the same order they were created in V1–V3.

DO $$
DECLARE
    tables TEXT[] := ARRAY[
        -- V1 core domain tables
        'users',
        'students',
        'enterprises',
        'openings',
        'applications',
        'pipeline_stages',
        'pipeline_entries',
        'express_interests',
        'chat_threads',
        'chat_thread_participants',
        'chat_messages',
        'calendar_events',
        'calendar_event_attendees',
        'notifications',
        'attachments',
        'comments',
        'activity_logs',
        'tags',
        'taggings',
        'success_stories',
        'feature_flags',
        'journal_entries',
        -- V2 auth tables
        'refresh_tokens',
        'password_reset_tokens',
        'email_verification_tokens',
        'oauth2_linked_accounts',
        'user_sessions',
        'login_attempts',
        -- V3 RBAC tables
        'roles',
        'permissions',
        'role_permissions',
        'user_roles',
        'enterprise_user_roles'
    ];
    t TEXT;
BEGIN
    FOREACH t IN ARRAY tables
    LOOP
        EXECUTE format('
            ALTER TABLE %I
                ADD COLUMN IF NOT EXISTS created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
                ADD COLUMN IF NOT EXISTS updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
                ADD COLUMN IF NOT EXISTS created_by  VARCHAR(255),
                ADD COLUMN IF NOT EXISTS updated_by  VARCHAR(255),
                ADD COLUMN IF NOT EXISTS version     BIGINT       NOT NULL DEFAULT 0,
                ADD COLUMN IF NOT EXISTS is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
                ADD COLUMN IF NOT EXISTS deleted_at  TIMESTAMPTZ,
                ADD COLUMN IF NOT EXISTS deleted_by  VARCHAR(255)
        ', t);
    END LOOP;
END;
$$;

-- ---------------------------------------------------------------------------
-- SOFT-DELETE INDEXES
-- Filter indexes on is_deleted make WHERE is_deleted = FALSE fast on every table.
-- ---------------------------------------------------------------------------

CREATE INDEX idx_users_not_deleted              ON users              (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_students_not_deleted           ON students           (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_enterprises_not_deleted        ON enterprises        (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_openings_not_deleted           ON openings           (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_applications_not_deleted       ON applications       (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_pipeline_stages_not_deleted    ON pipeline_stages    (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_pipeline_entries_not_deleted   ON pipeline_entries   (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_express_interests_not_deleted  ON express_interests  (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_notifications_not_deleted      ON notifications      (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_attachments_not_deleted        ON attachments        (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_comments_not_deleted           ON comments           (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_tags_not_deleted               ON tags               (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_success_stories_not_deleted    ON success_stories    (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_feature_flags_not_deleted      ON feature_flags      (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_journal_entries_not_deleted    ON journal_entries    (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_roles_not_deleted              ON roles              (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_permissions_not_deleted        ON permissions        (is_deleted) WHERE is_deleted = FALSE;

-- ---------------------------------------------------------------------------
-- TRIGGER FUNCTION: auto-update updated_at on every row change
-- ---------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

-- Attach the trigger to every domain table
DO $$
DECLARE
    tables TEXT[] := ARRAY[
        'users', 'students', 'enterprises', 'openings', 'applications',
        'pipeline_stages', 'pipeline_entries', 'express_interests',
        'chat_threads', 'chat_messages',
        'calendar_events', 'notifications', 'attachments', 'comments',
        'tags', 'success_stories', 'feature_flags', 'journal_entries',
        'refresh_tokens', 'password_reset_tokens', 'email_verification_tokens',
        'oauth2_linked_accounts', 'user_sessions',
        'roles', 'permissions'
    ];
    t TEXT;
BEGIN
    FOREACH t IN ARRAY tables
    LOOP
        EXECUTE format('
            CREATE TRIGGER trg_%s_updated_at
            BEFORE UPDATE ON %I
            FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at()
        ', replace(t, '.', '_'), t);
    END LOOP;
END;
$$;
