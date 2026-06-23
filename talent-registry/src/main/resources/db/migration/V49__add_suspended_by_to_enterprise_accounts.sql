-- Migration V49: Add suspended_by to enterprise_accounts

ALTER TABLE enterprise_accounts
ADD COLUMN suspended_by UUID;

-- Optionally, add foreign key constraint if users table exists in this context.
-- We use a soft reference since it's just an audit field and might point to a deleted HR user,
-- but typically we can add a constraint.
-- ALTER TABLE enterprise_accounts
-- ADD CONSTRAINT fk_enterprise_suspended_by FOREIGN KEY (suspended_by) REFERENCES users(id);
