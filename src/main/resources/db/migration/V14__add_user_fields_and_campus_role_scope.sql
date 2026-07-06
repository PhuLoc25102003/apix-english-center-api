-- V14__add_user_fields_and_campus_role_scope.sql
-- Add username and mfa_enabled to users, campus_id to user_roles, and seed RBAC

-- 1. Add fields to users
ALTER TABLE users ADD COLUMN username VARCHAR(255) UNIQUE;
ALTER TABLE users ADD COLUMN mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Add campus scope to user_roles
ALTER TABLE user_roles ADD COLUMN campus_id UUID REFERENCES campuses(id) ON DELETE SET NULL;
CREATE INDEX idx_user_roles_campus_id ON user_roles(campus_id);

-- 3. Modify unique constraint on user_roles
ALTER TABLE user_roles DROP CONSTRAINT uk_user_roles;

CREATE UNIQUE INDEX uk_user_roles_no_campus 
ON user_roles(user_id, role_id) 
WHERE campus_id IS NULL AND deleted_at IS NULL;

CREATE UNIQUE INDEX uk_user_roles_with_campus 
ON user_roles(user_id, role_id, campus_id) 
WHERE campus_id IS NOT NULL AND deleted_at IS NULL;

-- 4. Seed new/missing permissions
INSERT INTO permissions (code, module, action, description, is_active) VALUES
('user:deactivate', 'user', 'deactivate', 'Permission to deactivate users', TRUE),
('user:lock', 'user', 'lock', 'Permission to lock users', TRUE),
('user:unlock', 'user', 'unlock', 'Permission to unlock users', TRUE),
('user:reset-password', 'user', 'reset-password', 'Permission to reset user passwords', TRUE),
('user:assign-role', 'user', 'assign-role', 'Permission to assign roles to users', TRUE),
('user:remove-role', 'user', 'remove-role', 'Permission to remove roles from users', TRUE),
('role:deactivate', 'role', 'deactivate', 'Permission to deactivate roles', TRUE),
('role:assign-permission', 'role', 'assign-permission', 'Permission to assign permissions to roles', TRUE),
('role:remove-permission', 'role', 'remove-permission', 'Permission to remove permissions from roles', TRUE),
('permission:deactivate', 'permission', 'deactivate', 'Permission to deactivate permissions', TRUE)
ON CONFLICT (code) DO NOTHING;

-- 5. Map new permissions to SUPER_ADMIN and OWNER roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code IN ('SUPER_ADMIN', 'OWNER')
  AND p.code IN (
    'user:deactivate', 'user:lock', 'user:unlock', 'user:reset-password', 'user:assign-role', 'user:remove-role',
    'role:deactivate', 'role:assign-permission', 'role:remove-permission', 'permission:deactivate'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 6. Map broad permissions to CENTER_MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'CENTER_MANAGER'
  AND p.code IN (
    'user:read', 'user:create', 'user:update', 'user:deactivate', 'user:lock', 'user:unlock', 'user:reset-password', 'user:assign-role', 'user:remove-role',
    'role:read', 'permission:read'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;
