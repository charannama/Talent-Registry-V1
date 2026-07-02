-- =============================================================================
-- V14__reshape_user_roles_table.sql
--
-- Reshapes the user_roles table created in V3 so it aligns with
-- the UserRole JPA entity which extends BaseEntity.
--
-- V3 schema:
--   user_roles(user_id UUID, role_id UUID, PK=composite)
--
-- Target schema (matching BaseEntity + UserRole entity):
--   user_roles(
--     id            UUID PRIMARY KEY,
--     user_id       UUID NOT NULL REFERENCES users(id),
--     role_id       UUID NOT NULL REFERENCES roles(id),
--     created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
--     updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
--     created_by    VARCHAR(255),
--     updated_by    VARCHAR(255),
--     version       BIGINT NOT NULL DEFAULT 0,
--     is_deleted    BOOLEAN NOT NULL DEFAULT FALSE,
--     deleted_at    TIMESTAMP WITH TIME ZONE,
--     deleted_by    VARCHAR(255),
--     UNIQUE(user_id, role_id)   -- prevents duplicate active mappings
--   )
--
-- Strategy: rename the old table, recreate, migrate data, drop old.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Step 1: Rename the legacy junction table to preserve existing data
-- ---------------------------------------------------------------------------
ALTER TABLE user_roles RENAME TO user_roles_legacy_v3;
ALTER TABLE user_roles_legacy_v3 RENAME CONSTRAINT pk_user_roles TO pk_user_roles_legacy_v3;
ALTER TABLE user_roles_legacy_v3 DROP CONSTRAINT fk_ur_user;
ALTER TABLE user_roles_legacy_v3 DROP CONSTRAINT fk_ur_role;

-- ---------------------------------------------------------------------------
-- Step 2: Create the new user_roles table matching BaseEntity
-- ---------------------------------------------------------------------------
CREATE TABLE user_roles (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL,
    role_id       UUID         NOT NULL,

    -- BaseEntity audit columns
    created_at    TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT now(),
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255),
    version       BIGINT       NOT NULL DEFAULT 0,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP WITH TIME ZONE,
    deleted_by    VARCHAR(255),

    CONSTRAINT pk_user_roles             PRIMARY KEY (id),
    CONSTRAINT uq_user_role              UNIQUE (user_id, role_id),
    CONSTRAINT fk_ur_user                FOREIGN KEY (user_id)
                                             REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role                FOREIGN KEY (role_id)
                                             REFERENCES roles (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- Step 3: Migrate existing data from the legacy table.
--         Each legacy row gets a generated UUID and current-timestamp audits.
-- ---------------------------------------------------------------------------
INSERT INTO user_roles (
    id,
    user_id,
    role_id,
    created_at,
    updated_at,
    version,
    is_deleted
)
SELECT
    gen_random_uuid(),
    legacy.user_id,
    legacy.role_id,
    legacy.granted_at,
    now(),
    0,
    FALSE
FROM user_roles_legacy_v3 legacy;

-- ---------------------------------------------------------------------------
-- Step 4: Drop the legacy table now that data is migrated
-- ---------------------------------------------------------------------------
DROP TABLE user_roles_legacy_v3;

-- ---------------------------------------------------------------------------
-- Step 5: Performance indexes
-- ---------------------------------------------------------------------------
CREATE INDEX idx_ur_user_id_not_deleted
    ON user_roles (user_id)
    ;

CREATE INDEX idx_ur_role_id_not_deleted
    ON user_roles (role_id)
    ;

CREATE INDEX idx_ur_not_deleted_created_at
    ON user_roles (is_deleted, created_at DESC);

-- ---------------------------------------------------------------------------
-- Step 6: Auto-update trigger for updated_at
-- ---------------------------------------------------------------------------
-- CREATE TRIGGER trg_user_roles_updated_at
--    BEFORE UPDATE ON user_roles
--    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
