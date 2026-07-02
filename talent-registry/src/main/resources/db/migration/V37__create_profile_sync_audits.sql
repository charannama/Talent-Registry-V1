-- V37__create_profile_sync_audits.sql

CREATE TABLE profile_sync_audits
(
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    sync_start_time TIMESTAMP,
    sync_end_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_audit_profile FOREIGN KEY(profile_id) REFERENCES student_profiles(id)
);
