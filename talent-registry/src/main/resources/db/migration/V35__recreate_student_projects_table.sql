-- V35__recreate_student_projects_table.sql

DROP TABLE IF EXISTS student_projects;

CREATE TABLE student_projects
(
    id UUID PRIMARY KEY,

    profile_id UUID NOT NULL,

    project_name VARCHAR(255) NOT NULL,

    description TEXT,

    project_type VARCHAR(30) NOT NULL,

    domain VARCHAR(100) NOT NULL,

    completion_date TIMESTAMP,

    rubric_score INTEGER,

    mentor_feedback TEXT,

    technologies_used TEXT,

    repository_url VARCHAR(500),

    live_url VARCHAR(500),

    completed BOOLEAN DEFAULT TRUE,

    external_project_id VARCHAR(255),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    created_by VARCHAR(255),

    updated_by VARCHAR(255),

    version BIGINT NOT NULL DEFAULT 0,

    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,

    deleted_at TIMESTAMP,

    deleted_by VARCHAR(255),

    CONSTRAINT fk_project_profile
        FOREIGN KEY(profile_id)
        REFERENCES student_profiles(id)
);
