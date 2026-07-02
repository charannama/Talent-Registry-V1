-- =============================================================================
-- V3__role_permission_tables.sql
-- RBAC: roles, permissions, and their assignments
-- Covers: roles, permissions, role_permissions, user_roles,
--         enterprise_user_roles (scoped roles), seed data
-- =============================================================================

-- ---------------------------------------------------------------------------
-- ENUM: role_type  (mirrors com.zencube.registry.common.enums.RoleType)
-- ---------------------------------------------------------------------------

CREATE TYPE role_type AS ENUM (
    'SUPER_ADMIN',
    'ADMIN',
    'ENTERPRISE_ADMIN',
    'ENTERPRISE_RECRUITER',
    'RECRUITER',
    'HIRING_MANAGER',
    'STUDENT',
    'VIEWER',
    'SERVICE_ACCOUNT',
    'HR_STAFF',
    'CUSTOM'
);

-- ---------------------------------------------------------------------------
-- ROLES
-- ---------------------------------------------------------------------------

CREATE TABLE roles (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    role_type   role_type   NOT NULL,
    description TEXT,
    is_system   BOOLEAN     NOT NULL DEFAULT FALSE,   -- TRUE = cannot be deleted by users
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
);

-- ---------------------------------------------------------------------------
-- PERMISSIONS  (fine-grained action / resource pairs)
-- ---------------------------------------------------------------------------

CREATE TABLE permissions (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(150) NOT NULL,               -- e.g. "openings:create"
    resource    VARCHAR(100) NOT NULL,               -- e.g. "openings"
    action      VARCHAR(50)  NOT NULL,               -- e.g. "create"
    description TEXT,
    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uq_permissions_name UNIQUE (name)
);

-- ---------------------------------------------------------------------------
-- ROLE â†’ PERMISSIONS  (many-to-many)
-- ---------------------------------------------------------------------------

CREATE TABLE role_permissions (
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role       FOREIGN KEY (role_id)       REFERENCES roles (id)       ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- USER â†’ ROLES  (platform-level assignment)
-- ---------------------------------------------------------------------------

CREATE TABLE user_roles (
    user_id    UUID        NOT NULL,
    role_id    UUID        NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    granted_by UUID,                                 -- user_id of the admin who granted it
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user ON user_roles (user_id);
CREATE INDEX idx_user_roles_role ON user_roles (role_id);

-- ---------------------------------------------------------------------------
-- ENTERPRISE USER ROLES  (scoped role assignment within an enterprise)
-- A user can be a RECRUITER in Enterprise A and VIEWER in Enterprise B.
-- ---------------------------------------------------------------------------

CREATE TABLE enterprise_user_roles (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    enterprise_id UUID        NOT NULL,
    user_id       UUID        NOT NULL,
    role_id       UUID        NOT NULL,
    granted_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    granted_by    UUID,
    expires_at    TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_enterprise_user_roles PRIMARY KEY (id),
    CONSTRAINT uq_eur UNIQUE (enterprise_id, user_id, role_id),
    CONSTRAINT fk_eur_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprises (id) ON DELETE CASCADE,
    CONSTRAINT fk_eur_user       FOREIGN KEY (user_id)       REFERENCES users (id)        ON DELETE CASCADE,
    CONSTRAINT fk_eur_role       FOREIGN KEY (role_id)       REFERENCES roles (id)        ON DELETE CASCADE
);

CREATE INDEX idx_eur_enterprise ON enterprise_user_roles (enterprise_id);
CREATE INDEX idx_eur_user       ON enterprise_user_roles (user_id);

-- ---------------------------------------------------------------------------
-- SEED DATA â€“ system roles  (is_system = TRUE, never delete)
-- ---------------------------------------------------------------------------

INSERT INTO roles (id, name, role_type, description, is_system) VALUES
    (gen_random_uuid(), 'Super Admin',       'SUPER_ADMIN',       'Unrestricted platform access',                        TRUE),
    (gen_random_uuid(), 'Platform Admin',    'ADMIN',             'Manages platform-level settings and users',           TRUE),
    (gen_random_uuid(), 'Enterprise Admin',  'ENTERPRISE_ADMIN',  'Administers a single enterprise account',             TRUE),
    (gen_random_uuid(), 'Recruiter',         'RECRUITER',         'Posts openings and manages applications',             TRUE),
    (gen_random_uuid(), 'Hiring Manager',    'HIRING_MANAGER',    'Reviews and approves pipeline decisions',             TRUE),
    (gen_random_uuid(), 'Student',           'STUDENT',           'Registered job-seeker / talent profile',              TRUE),
    (gen_random_uuid(), 'Viewer',            'VIEWER',            'Read-only access to permitted resources',             TRUE),
    (gen_random_uuid(), 'Service Account',   'SERVICE_ACCOUNT',   'Machine-to-machine API access',                       TRUE);

-- ---------------------------------------------------------------------------
-- SEED DATA â€“ core permissions
-- ---------------------------------------------------------------------------

INSERT INTO permissions (id, name, resource, action, description) VALUES
    -- Users
    (gen_random_uuid(), 'users:read',            'users',        'read',    'View user profiles'),
    (gen_random_uuid(), 'users:create',          'users',        'create',  'Create new users'),
    (gen_random_uuid(), 'users:update',          'users',        'update',  'Update user profiles'),
    (gen_random_uuid(), 'users:delete',          'users',        'delete',  'Delete users'),

    -- Openings
    (gen_random_uuid(), 'openings:read',         'openings',     'read',    'View job openings'),
    (gen_random_uuid(), 'openings:create',       'openings',     'create',  'Create job openings'),
    (gen_random_uuid(), 'openings:update',       'openings',     'update',  'Update job openings'),
    (gen_random_uuid(), 'openings:delete',       'openings',     'delete',  'Delete job openings'),

    -- Applications
    (gen_random_uuid(), 'applications:read',     'applications', 'read',    'View applications'),
    (gen_random_uuid(), 'applications:create',   'applications', 'create',  'Submit applications'),
    (gen_random_uuid(), 'applications:update',   'applications', 'update',  'Update application status'),
    (gen_random_uuid(), 'applications:delete',   'applications', 'delete',  'Delete applications'),

    -- Enterprises
    (gen_random_uuid(), 'enterprises:read',      'enterprises',  'read',    'View enterprise profiles'),
    (gen_random_uuid(), 'enterprises:create',    'enterprises',  'create',  'Create enterprise profiles'),
    (gen_random_uuid(), 'enterprises:update',    'enterprises',  'update',  'Update enterprise profiles'),
    (gen_random_uuid(), 'enterprises:delete',    'enterprises',  'delete',  'Delete enterprise profiles'),

    -- Roles & Permissions (admin-only)
    (gen_random_uuid(), 'roles:read',            'roles',        'read',    'View roles'),
    (gen_random_uuid(), 'roles:manage',          'roles',        'manage',  'Create, update, delete roles'),
    (gen_random_uuid(), 'permissions:read',      'permissions',  'read',    'View permissions'),
    (gen_random_uuid(), 'permissions:manage',    'permissions',  'manage',  'Assign / revoke permissions'),

    -- Feature Flags
    (gen_random_uuid(), 'feature-flags:read',    'feature-flags','read',    'View feature flags'),
    (gen_random_uuid(), 'feature-flags:manage',  'feature-flags','manage',  'Toggle feature flags'),

    -- Reports / Analytics
    (gen_random_uuid(), 'reports:read',          'reports',      'read',    'View analytics and reports');
