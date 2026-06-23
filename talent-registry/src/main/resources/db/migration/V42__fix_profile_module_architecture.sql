-- V42__fix_profile_module_architecture.sql

-- 1. Fix missing base entity fields for student_profiles
ALTER TABLE student_profiles
ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMPTZ,
ADD COLUMN deleted_by VARCHAR(255);

-- 2. Fix missing base entity fields for student_skills
ALTER TABLE student_skills
ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMPTZ,
ADD COLUMN deleted_by VARCHAR(255);

ALTER TABLE student_profiles
  ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
  ALTER COLUMN updated_by TYPE VARCHAR(255) USING updated_by::text;

ALTER TABLE student_skills
  ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
  ALTER COLUMN updated_by TYPE VARCHAR(255) USING updated_by::text;

-- 3. Add missing indices
CREATE INDEX idx_student_projects_profile ON student_projects(profile_id);
CREATE INDEX idx_work_experiences_profile ON work_experiences(profile_id);
CREATE INDEX idx_student_skills_profile ON student_skills(profile_id);
CREATE INDEX idx_student_profiles_eligibility ON student_profiles(eligibility_level);

-- 4. Seed PROFILE_VIEW_ALL permission
INSERT INTO permissions (id, name, code, description, created_at, updated_at, version, is_deleted)
VALUES (gen_random_uuid(), 'PROFILE_VIEW_ALL', 'PROFILE_VIEW_ALL', 'View all student profiles', NOW(), NOW(), 0, FALSE)
ON CONFLICT DO NOTHING;

-- Note: In a production scenario, we'd also link this permission to the HR_STAFF and ADMIN roles in role_permissions,
-- assuming they are seeded via a script or managed dynamically.
-- We'll do a simple mapping if the roles exist.
INSERT INTO role_permissions (id, role_id, permission_id, created_at, updated_at, version, is_deleted)
SELECT gen_random_uuid(), r.id, p.id, NOW(), NOW(), 0, FALSE
FROM roles r
CROSS JOIN permissions p
WHERE r.name IN ('HR_STAFF', 'ADMIN', 'HR')
  AND p.name = 'PROFILE_VIEW_ALL'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp 
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
