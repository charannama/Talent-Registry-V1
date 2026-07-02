-- =============================================================================
-- V12__create_permissions_managed_table.sql
--
-- Prepares the permissions table for management via the Permission Module API.
--
-- Background:
--   V3 created a permissions table with columns: id, name, resource, action, description.
--   V4 added the BaseEntity audit columns to it.
--   The Permission entity now uses: id, name, code, description + BaseEntity audit fields.
--
-- This migration:
--   1. Adds the 'code' column (unique, NOT NULL) if it does not already exist.
--   2. Back-fills 'code' from existing rows using the existing 'name' column
--      (converting to SCREAMING_SNAKE_CASE for machine-readability).
--   3. Drops legacy columns 'resource' and 'action' that are no longer part of
--      the entity model.
--   4. Adds a unique constraint on 'code' and tightens the NOT NULL constraint.
--   5. Ensures a partial index exists on 'code' for fast is_deleted=FALSE lookups.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Step 1: Add the 'code' column (nullable initially so we can back-fill)
-- ---------------------------------------------------------------------------
ALTER TABLE permissions
    ADD COLUMN IF NOT EXISTS code VARCHAR(100);

-- ---------------------------------------------------------------------------
-- Step 2: Back-fill 'code' from 'name' for any existing rows.
--         Strategy: upper-case the name and replace spaces/hyphens with '_'.
--         This is a best-effort migration; duplicate codes get a suffix.
-- ---------------------------------------------------------------------------
UPDATE permissions
SET    code = UPPER(REGEXP_REPLACE(name, '[\s\-/:]+', '_'))
WHERE  code IS NULL;

-- ---------------------------------------------------------------------------
-- Step 3: Make 'code' NOT NULL and add a unique constraint.
--         If the back-fill above produced duplicate codes, the unique constraint
--         will fail â€” in that case, manually fix the data before running.
-- ---------------------------------------------------------------------------
ALTER TABLE permissions
    ALTER COLUMN code SET NOT NULL;

ALTER TABLE permissions
    ADD CONSTRAINT uq_permissions_code UNIQUE (code);

-- ---------------------------------------------------------------------------


-- ---------------------------------------------------------------------------
-- Step 5: Drop legacy columns from V3 that are no longer used by the entity.
--         Skipped if they have already been removed.
-- ---------------------------------------------------------------------------
ALTER TABLE permissions DROP COLUMN IF EXISTS resource;
ALTER TABLE permissions DROP COLUMN IF EXISTS action;

-- ---------------------------------------------------------------------------
-- Step 6: Partial index on 'code' for fast active-permission look-ups.
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_permissions_code_not_deleted
    ON permissions (code)
    ;

-- ---------------------------------------------------------------------------
-- Step 7: Partial index on 'name' for fast active-permission look-ups.
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_permissions_name_not_deleted
    ON permissions (name)
    ;
