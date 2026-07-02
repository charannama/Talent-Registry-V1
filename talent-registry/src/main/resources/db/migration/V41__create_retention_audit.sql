-- V41__create_retention_audit.sql

ALTER TABLE applications ADD COLUMN IF NOT EXISTS profile_id UUID;
ALTER TABLE applications ADD CONSTRAINT fk_applications_profile FOREIGN KEY (profile_id) REFERENCES student_profiles(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_applications_profile ON applications(profile_id);


CREATE TABLE profile_retention_audits
(
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    checked_at TIMESTAMP NOT NULL,
    checked_by VARCHAR(255),
    can_delete BOOLEAN NOT NULL,
    freeze_reason VARCHAR(50) NOT NULL,
    retention_expires_at TIMESTAMP,
    active_application_count INTEGER,
    most_advanced_status VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_profile_retention_audits_profile FOREIGN KEY(profile_id) REFERENCES student_profiles(id)
);

CREATE INDEX idx_profile_retention_audits_profile ON profile_retention_audits(profile_id);

