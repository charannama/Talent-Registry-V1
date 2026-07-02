-- Seed ENTERPRISE_RECRUITER role and add company_name to users table

INSERT INTO roles (id, name, role_type, description, is_system)
VALUES (gen_random_uuid(), 'ENTERPRISE_RECRUITER', 'ENTERPRISE_RECRUITER', 'Enterprise Recruiter Role', true)
;

ALTER TABLE users ADD COLUMN company_name VARCHAR(255);
