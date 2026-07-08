-- Complete the permission contract used by class staff, enrollment, schedules and attendance APIs.
INSERT INTO permissions(code,module,action,description,is_active)
SELECT code,split_part(code,':',1),split_part(code,':',2),'Permission to '||replace(code,':',' '),TRUE
FROM (VALUES
 ('class:read-assigned'),
 ('class-schedule:generate'),
 ('class-staff:read'),('class-staff:assign'),('class-staff:update'),('class-staff:remove'),
 ('class-student:read'),('class-student:assign'),('class-student:remove'),
 ('enrollment:read'),('enrollment:create'),('enrollment:update'),('enrollment:cancel'),
 ('attendance:read'),('attendance:mark'),('attendance:mark-all'),('attendance:complete'),
 ('attendance:monitor'),('attendance:office-override')
) v(code)
ON CONFLICT(code) DO UPDATE SET is_active=TRUE;

-- Administrators always receive every active permission, including permissions added later.
INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p
WHERE r.code IN ('SUPER_ADMIN','OWNER') AND r.deleted_at IS NULL AND p.deleted_at IS NULL
ON CONFLICT(role_id,permission_id) DO NOTHING;

-- Operational staff manage schedules, class assignments, enrollments and attendance.
INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p
WHERE r.code IN ('CENTER_MANAGER','OFFICE_STAFF')
  AND (p.module IN ('employee','position','curriculum','class-schedule','class-staff','class-student','enrollment')
       OR p.code IN ('attendance:read','attendance:mark','attendance:mark-all','attendance:complete','attendance:monitor','attendance:office-override'))
ON CONFLICT(role_id,permission_id) DO NOTHING;

-- Teachers and assistants operate only on classes enforced by class_staff scope checks.
INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p
WHERE r.code IN ('TEACHER','TEACHING_ASSISTANT')
  AND p.code IN ('class:read-assigned','class-staff:read','class-student:read','attendance:read','attendance:mark','attendance:mark-all','attendance:complete')
ON CONFLICT(role_id,permission_id) DO NOTHING;
