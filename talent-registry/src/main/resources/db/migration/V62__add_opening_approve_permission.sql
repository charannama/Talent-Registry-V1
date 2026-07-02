-- V62__add_opening_approve_permission.sql
-- Seed permissions for approving and viewing job openings

INSERT INTO permissions (id, name, code, description, created_at, updated_at, version, is_deleted)
VALUES 
(gen_random_uuid(), 'Approve Job Opening', 'OPENING_APPROVE', 'Allows HR to approve job openings', NOW(), NOW(), 0, FALSE),
(gen_random_uuid(), 'View All Job Openings', 'OPENING_VIEW_ALL', 'Allows viewing all live job openings', NOW(), NOW(), 0, FALSE)
;

-- Map OPENING_APPROVE to HR_STAFF
INSERT INTO role_permissions (id, role_id, permission_id, created_at, updated_at, version, is_deleted)
SELECT gen_random_uuid(), r.id, p.id, NOW(), NOW(), 0, FALSE
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'HR_STAFF'
  AND p.code = 'OPENING_APPROVE'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp 
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Map OPENING_APPROVE to Platform Admin (ADMIN)
INSERT INTO role_permissions (id, role_id, permission_id, created_at, updated_at, version, is_deleted)
SELECT gen_random_uuid(), r.id, p.id, NOW(), NOW(), 0, FALSE
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'Platform Admin'
  AND p.code = 'OPENING_APPROVE'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp 
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
