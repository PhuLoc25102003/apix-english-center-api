-- V17__deprecate_parent_student_login.sql
-- Deprecate parent/student login features, adjust session/attendance columns

-- 1. Drop login constraints and make columns nullable
ALTER TABLE students DROP CONSTRAINT IF EXISTS chk_students_user_id;
ALTER TABLE students ALTER COLUMN access_mode DROP NOT NULL;
ALTER TABLE students ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE parents ALTER COLUMN user_id DROP NOT NULL;

-- 2. Modify student_attendance to support NOT_MARKED and set as default
ALTER TABLE student_attendance DROP CONSTRAINT IF EXISTS chk_attendance_status;
ALTER TABLE student_attendance ADD CONSTRAINT chk_attendance_status 
    CHECK (status IN ('NOT_MARKED', 'PRESENT', 'ABSENT', 'LATE', 'EXCUSED'));
ALTER TABLE student_attendance ALTER COLUMN status SET DEFAULT 'NOT_MARKED';

-- 3. Modify class_sessions status and lesson_no constraints
ALTER TABLE class_sessions ALTER COLUMN lesson_no DROP NOT NULL;
ALTER TABLE class_sessions DROP CONSTRAINT IF EXISTS chk_sessions_lesson_no;

-- 4. Add room_id and schedule_id to class_sessions if not exists
ALTER TABLE class_sessions ADD COLUMN IF NOT EXISTS room_id UUID REFERENCES rooms(id) ON DELETE SET NULL;
ALTER TABLE class_sessions ADD COLUMN IF NOT EXISTS schedule_id UUID REFERENCES class_schedules(id) ON DELETE SET NULL;

-- 5. Delete Parent and Student roles and permissions
DELETE FROM role_permissions WHERE role_id IN (SELECT id FROM roles WHERE code IN ('PARENT', 'STUDENT'));
DELETE FROM roles WHERE code IN ('PARENT', 'STUDENT');
DELETE FROM permissions WHERE code IN ('dashboard:parent', 'dashboard:student');
