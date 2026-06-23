INSERT INTO roles (id, name, role_type, description, is_system)
VALUES (gen_random_uuid(), 'HR_STAFF', 'HR_STAFF', 'HR Staff member belonging to an enterprise', true)
ON CONFLICT (name) DO NOTHING;
