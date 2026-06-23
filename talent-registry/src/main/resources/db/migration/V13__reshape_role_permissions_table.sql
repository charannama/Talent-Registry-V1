-- =============================================================================
-- V13__reshape_role_permissions_table.sql
--
-- Reshapes the role_permissions table created in V3 so it aligns with
-- the RolePermission JPA entity which extends BaseEntity.
--
-- V3 schema:
--   role_permissions(role_id UUID, permission_id UUID, PK=composite)
--
-- Target schema (matching BaseEntity + RolePermission entity):
--   role_permissions(
--     id            UUID PRIMARY KEY,
--     role_id       UUID NOT NULL REFERENCES roles(id),
--     permission_id UUID NOT NULL REFERENCES permissions(id),
--     created_at    TIMESTAMPTZ NOT NULL,
--     updated_at    TIMESTAMPTZ NOT NULL,
--     created_by    VARCHAR(255),
--     updated_by    VARCHAR(255),
--     version       BIGINT NOT NULL DEFAULT 0,
--     is_deleted    BOOLEAN NOT NULL DEFAULT FALSE,
--     deleted_at    TIMESTAMPTZ,
--     deleted_by    VARCHAR(255),
--     UNIQUE(role_id, permission_id)   -- prevents duplicate active mappings
--   )
--
-- Strategy: rename the old table, recreate, migrate data, drop old.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Step 1: Rename the legacy junction table to preserve existing data
-- ---------------------------------------------------------------------------
ALTER TABLE role_permissions RENAME TO role_permissions_legacy_v3;
ALTER INDEX pk_role_permissions RENAME TO pk_role_permissions_legacy_v3;

-- ---------------------------------------------------------------------------
-- Step 2: Create the new role_permissions table matching BaseEntity
-- ---------------------------------------------------------------------------
CREATE TABLE role_permissions (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    role_id       UUID         NOT NULL,
    permission_id UUID         NOT NULL,

    -- BaseEntity audit columns
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255),
    version       BIGINT       NOT NULL DEFAULT 0,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMPTZ,
    deleted_by    VARCHAR(255),

    CONSTRAINT pk_role_permissions       PRIMARY KEY (id),
    CONSTRAINT uq_role_permission        UNIQUE (role_id, permission_id),
    CONSTRAINT fk_rp_role                FOREIGN KEY (role_id)
                                             REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission          FOREIGN KEY (permission_id)
                                             REFERENCES permissions (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- Step 3: Migrate existing data from the legacy table.
--         Each legacy row gets a generated UUID and current-timestamp audits.
-- ---------------------------------------------------------------------------
INSERT INTO role_permissions (
    id,
    role_id,
    permission_id,
    created_at,
    updated_at,
    version,
    is_deleted
)
SELECT
    gen_random_uuid(),
    legacy.role_id,
    legacy.permission_id,
    now(),
    now(),
    0,
    FALSE
FROM role_permissions_legacy_v3 legacy;

-- ---------------------------------------------------------------------------
-- Step 4: Drop the legacy table now that data is migrated
-- ---------------------------------------------------------------------------
DROP TABLE role_permissions_legacy_v3;

-- ---------------------------------------------------------------------------
-- Step 5: Performance indexes
-- ---------------------------------------------------------------------------
CREATE INDEX idx_rp_role_id_not_deleted
    ON role_permissions (role_id)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_rp_permission_id_not_deleted
    ON role_permissions (permission_id)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_rp_not_deleted_created_at
    ON role_permissions (is_deleted, created_at DESC);

-- ---------------------------------------------------------------------------
-- Step 6: Auto-update trigger for updated_at
-- ---------------------------------------------------------------------------
CREATE TRIGGER trg_role_permissions_updated_at
    BEFORE UPDATE ON role_permissions
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
