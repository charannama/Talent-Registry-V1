-- V70__create_missing_application_tables.sql
-- Aligns existing tables with JPA entities and creates new tables.

-- ============================================================================
-- 1. ALTER attachments: rename 'type' -> 'attachment_type', add 'profile_id'
--    Audit columns already exist from V4.
-- ============================================================================

ALTER TABLE attachments RENAME COLUMN type TO attachment_type;

ALTER TABLE attachments
    ALTER COLUMN attachment_type TYPE VARCHAR(50) USING attachment_type::text;

ALTER TABLE attachments
    ADD COLUMN IF NOT EXISTS profile_id UUID;

ALTER TABLE attachments
    ADD CONSTRAINT fk_attachments_profile FOREIGN KEY (profile_id) REFERENCES student_profiles(id);

-- ============================================================================
-- 2. ALTER applications: add new workflow columns
-- ============================================================================

ALTER TABLE applications
    ADD COLUMN IF NOT EXISTS applied_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS forwarded_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_stage_updated_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS resume_attachment_id UUID;

ALTER TABLE applications
    ADD CONSTRAINT fk_applications_resume FOREIGN KEY (resume_attachment_id) REFERENCES attachments(id);

-- ============================================================================
-- 3. CREATE application_status_history table
-- ============================================================================

CREATE TABLE application_status_history (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT      NOT NULL DEFAULT 0,
    is_deleted          BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMPTZ,
    deleted_by          VARCHAR(255),

    application_id      UUID        NOT NULL,
    status              VARCHAR(50) NOT NULL,
    changed_by_user_id  UUID,
    changed_at          TIMESTAMPTZ NOT NULL,
    notes               TEXT,

    CONSTRAINT fk_ash_application FOREIGN KEY (application_id) REFERENCES applications(id),
    CONSTRAINT fk_ash_changed_by  FOREIGN KEY (changed_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_ash_application_id ON application_status_history(application_id);
CREATE INDEX idx_ash_status         ON application_status_history(status);
CREATE INDEX idx_ash_changed_at     ON application_status_history(changed_at DESC);
