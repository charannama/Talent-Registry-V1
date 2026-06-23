-- =============================================================================
-- V24__add_custom_role_type.sql
-- Add CUSTOM value to the PostgreSQL role_type enum for custom admin-created roles.
-- =============================================================================

ALTER TYPE role_type ADD VALUE IF NOT EXISTS 'CUSTOM';
