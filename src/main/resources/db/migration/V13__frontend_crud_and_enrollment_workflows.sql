-- Frontend CRUD foundations and complete enrollment workflows.
ALTER TABLE class_enrollments DROP CONSTRAINT chk_enrollments_status;
ALTER TABLE class_enrollments ADD CONSTRAINT chk_enrollments_status
    CHECK (status IN ('TRIAL','ACTIVE','TRANSFERRED','FROZEN','COMPLETED','CANCELLED'));
ALTER TABLE class_enrollments ADD COLUMN cancellation_reason TEXT;
ALTER TABLE class_enrollments ADD CONSTRAINT chk_enrollment_dates CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date);

ALTER TABLE classes DROP CONSTRAINT chk_classes_status;
ALTER TABLE classes ADD CONSTRAINT chk_classes_status
    CHECK (status IN ('PLANNING','PLANNED','OPEN','ACTIVE','COMPLETED','CLOSED','CANCELLED'));

CREATE TABLE enrollment_transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_enrollment_id UUID NOT NULL REFERENCES class_enrollments(id) ON DELETE RESTRICT,
    to_enrollment_id UUID NOT NULL REFERENCES class_enrollments(id) ON DELETE RESTRICT,
    from_class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    to_class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    transfer_date DATE NOT NULL, reason TEXT NOT NULL,
    transferred_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE enrollment_freezes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), enrollment_id UUID NOT NULL REFERENCES class_enrollments(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL, end_date DATE NOT NULL, reason TEXT NOT NULL,
    previous_status VARCHAR(30) NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_freeze_dates CHECK (start_date <= end_date),
    CONSTRAINT chk_freeze_status CHECK (status IN ('ACTIVE','CANCELLED','COMPLETED'))
);

CREATE TABLE positions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), code VARCHAR(100) UNIQUE NOT NULL, name VARCHAR(150) NOT NULL,
    description TEXT, is_teaching_position BOOLEAN NOT NULL DEFAULT FALSE, is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_position_code CHECK (code ~ '^[A-Z][A-Z0-9_]*$')
);

ALTER TABLE employees ADD COLUMN date_of_birth DATE;
ALTER TABLE employees ADD COLUMN gender VARCHAR(20);
ALTER TABLE employees ADD COLUMN address TEXT;
ALTER TABLE employees ADD COLUMN emergency_contact_name VARCHAR(255);
ALTER TABLE employees ADD COLUMN emergency_contact_phone VARCHAR(30);
ALTER TABLE employees ADD COLUMN hired_date DATE NOT NULL DEFAULT CURRENT_DATE;
ALTER TABLE employees ADD COLUMN resigned_date DATE;
ALTER TABLE employees ADD COLUMN campus_id UUID REFERENCES campuses(id) ON DELETE SET NULL;
ALTER TABLE employees ADD COLUMN note TEXT;
ALTER TABLE employees ADD CONSTRAINT chk_employee_work_dates CHECK (resigned_date IS NULL OR resigned_date >= hired_date);

CREATE TABLE employee_positions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    position_id UUID NOT NULL REFERENCES positions(id) ON DELETE RESTRICT,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE, effective_from DATE NOT NULL DEFAULT CURRENT_DATE, effective_to DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_employee_position UNIQUE(employee_id,position_id,effective_from),
    CONSTRAINT chk_employee_position_dates CHECK(effective_to IS NULL OR effective_to >= effective_from)
);

CREATE TABLE curriculums (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), course_id UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL, version_name VARCHAR(100) NOT NULL, description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_curriculum_course_version UNIQUE(course_id,version_name)
);

CREATE TABLE lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), curriculum_id UUID NOT NULL REFERENCES curriculums(id) ON DELETE RESTRICT,
    lesson_no INTEGER NOT NULL, title VARCHAR(255) NOT NULL, content TEXT, duration_minutes INTEGER NOT NULL DEFAULT 90,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_curriculum_lesson UNIQUE(curriculum_id,lesson_no),
    CONSTRAINT chk_lesson_values CHECK(lesson_no > 0 AND duration_minutes > 0)
);

CREATE TABLE class_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT, day_of_week INTEGER NOT NULL,
    start_time TIME NOT NULL, end_time TIME NOT NULL, effective_from DATE NOT NULL, effective_to DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', pattern_code VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_class_schedule_day CHECK(day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_class_schedule_time CHECK(start_time < end_time),
    CONSTRAINT chk_class_schedule_dates CHECK(effective_to IS NULL OR effective_to >= effective_from),
    CONSTRAINT chk_class_schedule_status CHECK(status IN ('ACTIVE','INACTIVE')),
    CONSTRAINT uq_class_schedule_slot UNIQUE(class_id,day_of_week,start_time,effective_from)
);

INSERT INTO permissions(code,module,action,description,is_active)
SELECT code,split_part(code,':',1),split_part(code,':',2),'Permission to '||replace(code,':',' '),TRUE
FROM (VALUES
 ('enrollment:cancel'),('enrollment:transfer'),('enrollment:freeze'),('enrollment:complete'),('enrollment:override-capacity'),('enrollment:read-parent-info'),('enrollment:export'),
 ('curriculum:read'),('curriculum:create'),('curriculum:update'),('curriculum:delete'),
 ('position:read'),('position:create'),('position:update'),('position:delete'),
 ('employee:read'),('employee:create'),('employee:update'),('employee:delete'),('employee:resign'),
 ('class-schedule:read'),('class-schedule:create'),('class-schedule:update'),('class-schedule:delete'),
 ('level:read'),('user:read')
) v(code) ON CONFLICT(code) DO NOTHING;

INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p WHERE r.code IN ('SUPER_ADMIN','OWNER')
ON CONFLICT(role_id,permission_id) DO NOTHING;
INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p
WHERE r.code IN ('CENTER_MANAGER','OFFICE_STAFF') AND p.module IN ('enrollment','curriculum','position','employee','class-schedule')
ON CONFLICT(role_id,permission_id) DO NOTHING;

CREATE INDEX idx_enrollment_filters ON class_enrollments(status,class_id,student_id,enrolled_date);
CREATE INDEX idx_enrollment_source ON class_enrollments(source);
CREATE INDEX idx_enrollment_transfers_from ON enrollment_transfers(from_enrollment_id,transfer_date);
CREATE INDEX idx_enrollment_freezes_enrollment ON enrollment_freezes(enrollment_id,start_date);
CREATE INDEX idx_employee_positions_employee ON employee_positions(employee_id,position_id);
CREATE INDEX idx_curriculums_course_active ON curriculums(course_id,is_active);
CREATE INDEX idx_class_schedules_filters ON class_schedules(class_id,room_id,day_of_week,status);
