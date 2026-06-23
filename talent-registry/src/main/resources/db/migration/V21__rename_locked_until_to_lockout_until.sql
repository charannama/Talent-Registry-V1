-- Rename locked_until to lockout_until in users table

ALTER TABLE public.users
RENAME COLUMN locked_until TO lockout_until;
