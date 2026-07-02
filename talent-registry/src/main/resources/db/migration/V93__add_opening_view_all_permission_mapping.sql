-- V71__add_opening_view_all_permission_mapping.sql
-- Map OPENING_VIEW_ALL to HR_STAFF, ENTERPRISE_ADMIN, and Platform Admin

INSERT INTO role_permissions (id, role_id, permission_id, created_at, updated_at, version, is_deleted)
SELECT gen_random_uuid(), r.id, p.id, NOW(), NOW(), 0, FALSE
FROM roles r
CROSS JOIN permissions p
WHERE r.name IN ('HR_STAFF', 'ENTERPRISE_ADMIN', 'Platform Admin')
  AND p.code = 'OPENING_VIEW_ALL'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp 
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
