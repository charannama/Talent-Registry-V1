-- V32__create_student_skills.sql

CREATE TABLE student_skills (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    skill_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID
);
