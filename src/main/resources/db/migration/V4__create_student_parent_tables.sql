-- V4__create_student_parent_tables.sql
-- Create student and parent management tables

-- 1. Create parents table
CREATE TABLE parents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    parent_code VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(255),
    address TEXT,
    job_title VARCHAR(255),
    note TEXT,
    
    -- common columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0
);

-- 2. Create students table
CREATE TABLE students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    student_code VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20),
    school_name VARCHAR(255),
    grade VARCHAR(50),
    avatar_url TEXT,
    medical_notes TEXT,
    learning_notes TEXT,
    student_type VARCHAR(30) NOT NULL,
    access_mode VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    
    -- common columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    
    -- constraints
    CONSTRAINT chk_students_student_type CHECK (student_type IN ('KINDERGARTEN', 'CHILD', 'TEENAGER', 'ADULT')),
    CONSTRAINT chk_students_access_mode CHECK (access_mode IN ('NO_ACCOUNT', 'PARENT_MANAGED', 'OWN_ACCOUNT')),
    CONSTRAINT chk_students_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'GRADUATED', 'PAUSED')),
    CONSTRAINT chk_students_user_id CHECK (
        (access_mode = 'OWN_ACCOUNT' AND user_id IS NOT NULL)
        OR
        (access_mode IN ('NO_ACCOUNT', 'PARENT_MANAGED'))
    )
);

-- 3. Create student_parents relationship table
CREATE TABLE student_parents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    parent_id UUID NOT NULL REFERENCES parents(id) ON DELETE CASCADE,
    relationship VARCHAR(50) NOT NULL,
    is_primary_contact BOOLEAN DEFAULT FALSE,
    can_receive_notification BOOLEAN DEFAULT TRUE,
    can_receive_tuition BOOLEAN DEFAULT TRUE,
    can_pickup_student BOOLEAN DEFAULT FALSE,
    is_emergency_contact BOOLEAN DEFAULT FALSE,
    
    -- common columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    
    -- constraints
    CONSTRAINT uk_student_parents UNIQUE (student_id, parent_id),
    CONSTRAINT chk_student_parents_relationship CHECK (relationship IN ('FATHER', 'MOTHER', 'GUARDIAN', 'SIBLING', 'OTHER'))
);

-- 4. Create performance indexes
-- Soft delete indexes
CREATE INDEX idx_students_deleted_at ON students(deleted_at);
CREATE INDEX idx_parents_deleted_at ON parents(deleted_at);
CREATE INDEX idx_student_parents_deleted_at ON student_parents(deleted_at);

-- Foreign key search indexes
CREATE INDEX idx_students_user_id ON students(user_id);
CREATE INDEX idx_parents_user_id ON parents(user_id);
CREATE INDEX idx_student_parents_student_id ON student_parents(student_id);
CREATE INDEX idx_student_parents_parent_id ON student_parents(parent_id);

-- Code indexes for direct lookup
CREATE INDEX idx_students_student_code ON students(student_code);
CREATE INDEX idx_parents_parent_code ON parents(parent_code);
