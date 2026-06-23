-- V59__create_job_openings.sql
-- Create job openings table and seed permissions

CREATE TABLE job_openings (
    id UUID NOT NULL,
    enterprise_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    requirements TEXT,
    location VARCHAR(255),
    job_type VARCHAR(50),
    domain VARCHAR(100),
    salary_min NUMERIC(19, 2),
    salary_max NUMERIC(19, 2),
    work_mode VARCHAR(50),
    positions INTEGER,
    application_deadline TIMESTAMPTZ,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    required_skills VARCHAR(1000),
    graduation_years VARCHAR(255),
    
    -- BaseEntity audit columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(255),

    CONSTRAINT pk_job_openings PRIMARY KEY (id),
    CONSTRAINT fk_job_openings_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise_accounts(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_job_openings_enterprise_id ON job_openings(enterprise_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_job_openings_status ON job_openings(status) WHERE is_deleted = FALSE;

-- Unique constraint ensuring an enterprise cannot have duplicate active opening titles
CREATE UNIQUE INDEX uq_enterprise_opening_title_active ON job_openings(enterprise_id, title) WHERE is_deleted = FALSE;

-- Seed permission
INSERT INTO permissions (id, name, code, description, created_at, updated_at, version, is_deleted)
VALUES (gen_random_uuid(), 'Create Job Opening', 'OPENING_CREATE', 'Allows creation of job openings', NOW(), NOW(), 0, FALSE)
ON CONFLICT (code) DO NOTHING;

-- Map permission to roles: ENTERPRISE_RECRUITER, HR_STAFF, Platform Admin (ADMIN)
-- Map to ENTERPRISE_RECRUITER
INSERT INTO role_permissions (id, role_id, permission_id, created_at, updated_at, version, is_deleted)
SELECT gen_random_uuid(), r.id, p.id, NOW(), NOW(), 0, FALSE
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ENTERPRISE_RECRUITER'
  AND p.code = 'OPENING_CREATE'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp 
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Map to HR_STAFF
INSERT INTO role_permissions (id, role_id, permission_id, created_at, updated_at, version, is_deleted)
SELECT gen_random_uuid(), r.id, p.id, NOW(), NOW(), 0, FALSE
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'HR_STAFF'
  AND p.code = 'OPENING_CREATE'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp 
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Map to Platform Admin (ADMIN)
INSERT INTO role_permissions (id, role_id, permission_id, created_at, updated_at, version, is_deleted)
SELECT gen_random_uuid(), r.id, p.id, NOW(), NOW(), 0, FALSE
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'Platform Admin'
  AND p.code = 'OPENING_CREATE'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp 
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
