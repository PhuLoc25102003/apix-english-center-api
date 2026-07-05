-- V8__create_attendance_tables.sql
-- Create class_sessions and student_attendance tables

-- 1. Create class_sessions table
CREATE TABLE class_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    lesson_no INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    note TEXT,
    
    -- common audit columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    
    -- constraints
    CONSTRAINT chk_sessions_time CHECK (start_time < end_time),
    CONSTRAINT chk_sessions_lesson_no CHECK (lesson_no > 0)
);

-- 2. Create student_attendance table
CREATE TABLE student_attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES class_sessions(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE RESTRICT,
    status VARCHAR(30) NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    note TEXT,
    marked_by UUID REFERENCES users(id) ON DELETE SET NULL,
    marked_at TIMESTAMPTZ,
    
    -- common audit columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    
    -- constraints
    CONSTRAINT chk_attendance_status CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED')),
    CONSTRAINT uq_session_student UNIQUE (session_id, student_id)
);

-- 3. Create indexes
CREATE INDEX idx_class_sessions_class_id ON class_sessions(class_id);
CREATE INDEX idx_class_sessions_session_date ON class_sessions(session_date);
CREATE INDEX idx_class_sessions_deleted_at ON class_sessions(deleted_at);

CREATE INDEX idx_student_attendance_session_id ON student_attendance(session_id);
CREATE INDEX idx_student_attendance_student_id ON student_attendance(student_id);
CREATE INDEX idx_student_attendance_status ON student_attendance(status);
CREATE INDEX idx_student_attendance_deleted_at ON student_attendance(deleted_at);
