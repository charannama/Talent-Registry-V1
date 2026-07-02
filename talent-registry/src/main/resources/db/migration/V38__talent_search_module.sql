-- V38__talent_search_module.sql

ALTER TABLE student_profiles
ADD COLUMN profile_visible BOOLEAN DEFAULT TRUE;
ALTER TABLE student_profiles ADD COLUMN suspended BOOLEAN DEFAULT FALSE;
ALTER TABLE student_profiles ADD COLUMN profile_views BIGINT DEFAULT 0;
ALTER TABLE student_profiles ADD COLUMN highest_project_type VARCHAR(50);
ALTER TABLE student_profiles ADD COLUMN searchable BOOLEAN DEFAULT TRUE;
ALTER TABLE student_profiles ADD COLUMN talent_qualified BOOLEAN DEFAULT FALSE;

CREATE INDEX idx_student_profiles_highest_proj ON student_profiles(highest_project_type);
CREATE INDEX idx_student_profiles_talent_qual ON student_profiles(talent_qualified);
CREATE INDEX idx_student_profiles_searchable ON student_profiles(searchable);

CREATE TABLE talent_profile_views
(
    id UUID PRIMARY KEY,
    enterprise_id UUID NOT NULL,
    profile_id UUID NOT NULL,
    viewed_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_talent_profile_views_profile FOREIGN KEY(profile_id) REFERENCES student_profiles(id)
);

CREATE INDEX idx_talent_profile_views_profile ON talent_profile_views(profile_id);
CREATE INDEX idx_talent_profile_views_enterprise ON talent_profile_views(enterprise_id);
