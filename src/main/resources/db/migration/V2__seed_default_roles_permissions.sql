-- V2__seed_default_roles_permissions.sql
-- Seed default roles and permissions for APIX English Center

-- 1. Insert default roles
INSERT INTO roles (code, name, description, is_system, is_active) VALUES
('OWNER', 'Owner', 'System Owner with full access permissions', TRUE, TRUE),
('OFFICE_STAFF', 'Office Staff', 'Office Staff responsible for managing center operations, students, parents, enrollments, tuition, and attendance', TRUE, TRUE),
('TEACHER', 'Teacher', 'Teacher responsible for conducting classes, tracking attendance, and evaluating students', TRUE, TRUE),
('TEACHING_ASSISTANT', 'Teaching Assistant', 'Teaching Assistant supporting teachers in classrooms and student management', TRUE, TRUE),
('PARENT', 'Parent', 'Parent or Guardian profile access', TRUE, TRUE),
('STUDENT', 'Student', 'Student profile access', TRUE, TRUE)
ON CONFLICT (code) DO NOTHING;

-- 2. Insert permissions for modules and actions
-- We generate permissions dynamically using a cross join of modules and actions.
INSERT INTO permissions (code, module, action, description, is_active)
SELECT 
    m.module_name || ':' || a.action_name,
    m.module_name,
    a.action_name,
    'Permission to ' || a.action_name || ' ' || m.module_name,
    TRUE
FROM 
    (VALUES 
        ('user'), ('role'), ('permission'), ('student'), ('parent'), 
        ('campus'), ('room'), ('course'), ('class'), ('enrollment'), 
        ('attendance'), ('tuition'), ('contactlog')
    ) AS m(module_name)
CROSS JOIN 
    (VALUES 
        ('read'), ('create'), ('update'), ('delete'), ('approve'), ('collect'), ('mark')
    ) AS a(action_name)
ON CONFLICT (code) DO NOTHING;

-- 3. Assign permissions to roles

-- OWNER: Assign all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'OWNER'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- OFFICE_STAFF:
-- Can manage (all actions): student, parent, enrollment, attendance, tuition, contactlog
-- Can view (read): user, role, permission, campus, room, course, class
-- Can also perform operational tasks for campus, room, course, class: create, update, approve, mark
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'OFFICE_STAFF'
  AND (
    -- Full management for operations
    p.module IN ('student', 'parent', 'enrollment', 'attendance', 'tuition', 'contactlog')
    OR
    -- Operations on campus, room, course, class (excluding delete for safety)
    (p.module IN ('campus', 'room', 'course', 'class') AND p.action IN ('read', 'create', 'update', 'approve', 'mark'))
    OR
    -- Read-only for system setup
    (p.module IN ('user', 'role', 'permission') AND p.action = 'read')
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- TEACHER:
-- Can mark/manage attendance, view classes/courses/rooms/campuses/students/parents/enrollments
-- Must NOT have parent communication (contactlog) or tuition collection (tuition) permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'TEACHER'
  AND (
    -- View operations
    (p.module IN ('campus', 'room', 'course', 'class', 'student', 'parent', 'enrollment') AND p.action = 'read')
    OR
    -- Manage attendance (read, create, update, mark)
    (p.module = 'attendance' AND p.action IN ('read', 'create', 'update', 'mark'))
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- TEACHING_ASSISTANT:
-- Can view classes/courses/rooms/campuses/students/parents/enrollments
-- Can mark/view attendance
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'TEACHING_ASSISTANT'
  AND (
    (p.module IN ('campus', 'room', 'course', 'class', 'student', 'parent', 'enrollment') AND p.action = 'read')
    OR
    (p.module = 'attendance' AND p.action IN ('read', 'mark'))
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- PARENT:
-- Can view own/child data (student, parent, class, attendance, tuition, enrollment, campus, room, course)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'PARENT'
  AND p.module IN ('student', 'parent', 'class', 'attendance', 'tuition', 'enrollment', 'campus', 'room', 'course')
  AND p.action = 'read'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- STUDENT:
-- Can view own data (student, class, attendance, tuition, enrollment, campus, room, course)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'STUDENT'
  AND p.module IN ('student', 'class', 'attendance', 'tuition', 'enrollment', 'campus', 'room', 'course')
  AND p.action = 'read'
ON CONFLICT (role_id, permission_id) DO NOTHING;
