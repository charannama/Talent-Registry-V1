-- V36__recreate_work_experiences_table.sql

DROP TABLE IF EXISTS student_experiences;

CREATE TABLE work_experiences
(
    id UUID PRIMARY KEY,

    profile_id UUID NOT NULL,

    job_title VARCHAR(255) NOT NULL,

    company_name VARCHAR(255) NOT NULL,

    location VARCHAR(255),

    employment_type VARCHAR(50),

    start_date TIMESTAMP,

    end_date TIMESTAMP,

    currently_working BOOLEAN DEFAULT FALSE,

    description TEXT,

    key_responsibilities TEXT,

    external_experience_id VARCHAR(255),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    created_by VARCHAR(255),

    updated_by VARCHAR(255),

    version BIGINT NOT NULL DEFAULT 0,

    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,

    deleted_at TIMESTAMP,

    deleted_by VARCHAR(255),

    CONSTRAINT fk_experience_profile
        FOREIGN KEY(profile_id)
        REFERENCES student_profiles(id)
);
