-- Add password_changed_at column to users table

ALTER TABLE public.users
ADD COLUMN password_changed_at TIMESTAMP WITH TIME ZONE;
