-- V7__create_class_enrollments_table.sql
-- Create class_enrollments table and indexes

CREATE TABLE class_enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE RESTRICT,
    enrollment_code VARCHAR(50) UNIQUE NOT NULL,
    enrolled_date DATE NOT NULL DEFAULT CURRENT_DATE,
    start_date DATE,
    end_date DATE,
    status VARCHAR(30) NOT NULL,
    source VARCHAR(50),
    note TEXT,
    
    -- common audit columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    
    -- constraints
    CONSTRAINT chk_enrollments_status CHECK (status IN ('ACTIVE', 'TRANSFERRED', 'FROZEN', 'COMPLETED', 'CANCELLED'))
);

-- Indexes
CREATE INDEX idx_class_enrollments_class_id ON class_enrollments(class_id);
CREATE INDEX idx_class_enrollments_student_id ON class_enrollments(student_id);
CREATE INDEX idx_class_enrollments_status ON class_enrollments(status);
CREATE INDEX idx_class_enrollments_deleted_at ON class_enrollments(deleted_at);

-- Business rule unique index: A student cannot have two active enrollments in the same class
CREATE UNIQUE INDEX uq_active_class_enrollment 
ON class_enrollments (student_id, class_id) 
WHERE (status = 'ACTIVE' AND deleted_at IS NULL);
