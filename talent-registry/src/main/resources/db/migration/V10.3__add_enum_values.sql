-- Add new role types to the role_type enum

ALTER TYPE role_type ADD VALUE IF NOT EXISTS 'HR_STAFF';
ALTER TYPE role_type ADD VALUE IF NOT EXISTS 'ENTERPRISE_RECRUITER';
