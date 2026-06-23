-- V34__create_student_experiences.sql

CREATE TABLE student_experiences (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,
    currently_working BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID
);
