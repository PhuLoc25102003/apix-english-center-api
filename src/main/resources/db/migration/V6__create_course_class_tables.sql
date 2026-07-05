-- V6__create_course_class_tables.sql
-- Create levels, courses, and classes management tables

-- 1. Create levels table
CREATE TABLE levels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    order_index INTEGER DEFAULT 0 NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    
    -- common audit columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0
);

-- 2. Create courses table
CREATE TABLE courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    level_id UUID REFERENCES levels(id) ON DELETE SET NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    total_lessons INTEGER NOT NULL,
    duration_minutes INTEGER NOT NULL,
    default_tuition_fee DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL,
    
    -- common audit columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    
    -- constraints
    CONSTRAINT chk_courses_total_lessons CHECK (total_lessons > 0),
    CONSTRAINT chk_courses_duration_minutes CHECK (duration_minutes > 0),
    CONSTRAINT chk_courses_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'))
);

-- 3. Create classes table
CREATE TABLE classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID REFERENCES courses(id) ON DELETE SET NULL,
    campus_id UUID REFERENCES campuses(id) ON DELETE SET NULL,
    class_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    capacity INTEGER NOT NULL,
    start_date DATE NOT NULL,
    expected_end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    note TEXT,
    
    -- common audit columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    
    -- constraints
    CONSTRAINT chk_classes_capacity CHECK (capacity > 0),
    CONSTRAINT chk_classes_dates CHECK (start_date <= expected_end_date),
    CONSTRAINT chk_classes_status CHECK (status IN ('PLANNING', 'OPEN', 'ACTIVE', 'CLOSED', 'CANCELLED'))
);

-- 4. Create performance indexes
-- Soft delete indexes
CREATE INDEX idx_levels_deleted_at ON levels(deleted_at);
CREATE INDEX idx_courses_deleted_at ON courses(deleted_at);
CREATE INDEX idx_classes_deleted_at ON classes(deleted_at);

-- Foreign key search indexes
CREATE INDEX idx_courses_level_id ON courses(level_id);
CREATE INDEX idx_classes_course_id ON classes(course_id);
CREATE INDEX idx_classes_campus_id ON classes(campus_id);

-- Code indexes for direct lookup
CREATE INDEX idx_levels_code ON levels(code);
CREATE INDEX idx_courses_code ON courses(code);
CREATE INDEX idx_classes_class_code ON classes(class_code);
