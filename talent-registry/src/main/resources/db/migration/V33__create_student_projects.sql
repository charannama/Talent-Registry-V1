-- V33__create_student_projects.sql

CREATE TABLE student_projects (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    repository_url VARCHAR(500),
    live_url VARCHAR(500),
    completed BOOLEAN DEFAULT FALSE,
    project_level INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID
);
