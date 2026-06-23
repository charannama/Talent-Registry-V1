-- Adds timezone setting to users table

ALTER TABLE users ADD COLUMN timezone VARCHAR(50) DEFAULT 'UTC';
