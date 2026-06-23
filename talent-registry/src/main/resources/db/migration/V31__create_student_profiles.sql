-- V31__create_student_profiles.sql

CREATE TABLE student_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    avatar_url VARCHAR(500),
    institution VARCHAR(255),
    discipline VARCHAR(255),
    graduation_year INT,
    gpa DOUBLE PRECISION,
    coursework VARCHAR(1000),
    location VARCHAR(255),
    linkedin_url VARCHAR(500),
    github_url VARCHAR(500),
    portfolio_url VARCHAR(500),
    full_time_ready BOOLEAN DEFAULT FALSE,
    internship_ready BOOLEAN DEFAULT FALSE,
    remote_preference BOOLEAN DEFAULT FALSE,
    eligibility_level VARCHAR(50) NOT NULL DEFAULT 'NO_PROJECT',
    last_sync_at TIMESTAMPTZ,
    sync_status VARCHAR(50) NOT NULL DEFAULT 'NEVER_SYNCED',
    sync_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID
);
