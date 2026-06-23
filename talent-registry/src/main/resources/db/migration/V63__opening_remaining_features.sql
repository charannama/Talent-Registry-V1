-- V63__opening_remaining_features.sql
-- Add rejection fields and remaining permissions for Job Openings

ALTER TABLE job_openings
ADD COLUMN rejected_by UUID,
ADD COLUMN rejected_at TIMESTAMPTZ,
ADD COLUMN rejection_reason TEXT;

-- Seed remaining permissions
INSERT INTO permissions (id, name, code, description, created_at, updated_at, version, is_deleted)
VALUES 
(gen_random_uuid(), 'Update Job Opening', 'OPENING_UPDATE', 'Allows enterprise to update draft job openings', NOW(), NOW(), 0, FALSE),
(gen_random_uuid(), 'Close Job Opening', 'OPENING_CLOSE', 'Allows enterprise to close live job openings', NOW(), NOW(), 0, FALSE),
(gen_random_uuid(), 'Archive Job Opening', 'OPENING_ARCHIVE', 'Allows enterprise to archive closed job openings', NOW(), NOW(), 0, FALSE),
(gen_random_uuid(), 'View Job Opening', 'OPENING_VIEW', 'Allows viewing specific job openings', NOW(), NOW(), 0, FALSE)
ON CONFLICT (code) DO NOTHING;

-- Map OPENING_UPDATE, OPENING_CLOSE, OPENING_ARCHIVE, OPENING_VIEW to ENTERPRISE_RECRUITER
INSERT INTO role_permissions (id, role_id, permission_id, created_at, updated_at, version, is_deleted)
SELECT gen_random_uuid(), r.id, p.id, NOW(), NOW(), 0, FALSE
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ENTERPRISE_RECRUITER'
  AND p.code IN ('OPENING_UPDATE', 'OPENING_CLOSE', 'OPENING_ARCHIVE', 'OPENING_VIEW')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp 
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Map to Platform Admin
INSERT INTO role_permissions (id, role_id, permission_id, created_at, updated_at, version, is_deleted)
SELECT gen_random_uuid(), r.id, p.id, NOW(), NOW(), 0, FALSE
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'Platform Admin'
  AND p.code IN ('OPENING_UPDATE', 'OPENING_CLOSE', 'OPENING_ARCHIVE', 'OPENING_VIEW')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp 
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
