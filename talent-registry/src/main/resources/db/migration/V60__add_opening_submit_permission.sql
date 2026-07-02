-- V60__add_opening_submit_permission.sql
-- Seed permission for submitting job openings

INSERT INTO permissions (id, name, code, description, created_at, updated_at, version, is_deleted)
VALUES (gen_random_uuid(), 'Submit Job Opening', 'OPENING_SUBMIT', 'Allows submitting job openings for HR approval', NOW(), NOW(), 0, FALSE)
;

-- Map permission to roles: ENTERPRISE_RECRUITER
INSERT INTO role_permissions (id, role_id, permission_id, created_at, updated_at, version, is_deleted)
SELECT gen_random_uuid(), r.id, p.id, NOW(), NOW(), 0, FALSE
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ENTERPRISE_RECRUITER'
  AND p.code = 'OPENING_SUBMIT'
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
  AND p.code = 'OPENING_SUBMIT'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp 
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
