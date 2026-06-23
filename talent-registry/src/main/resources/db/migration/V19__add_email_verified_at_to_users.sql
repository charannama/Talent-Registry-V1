-- Add email_verified_at column to users table

ALTER TABLE public.users
ADD COLUMN email_verified_at TIMESTAMP WITH TIME ZONE;
