-- V43__create_profile_access_audit.sql

CREATE TABLE profile_access_audits
(
    id UUID PRIMARY KEY,
    viewer_user_id UUID NOT NULL,
    target_user_id UUID NOT NULL,
    access_reason VARCHAR(50) NOT NULL,
    access_result VARCHAR(50) NOT NULL,
    ip_address VARCHAR(255),
    user_agent VARCHAR(500),
    accessed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX idx_profile_access_viewer ON profile_access_audits(viewer_user_id);
CREATE INDEX idx_profile_access_target ON profile_access_audits(target_user_id);
CREATE INDEX idx_profile_access_time ON profile_access_audits(accessed_at);
