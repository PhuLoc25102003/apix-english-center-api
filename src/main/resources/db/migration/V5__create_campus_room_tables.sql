-- V5__create_campus_room_tables.sql
-- Create campuses and rooms management tables

-- 1. Create campuses table
CREATE TABLE campuses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    phone VARCHAR(30),
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

-- 2. Create rooms table
CREATE TABLE rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campus_id UUID NOT NULL REFERENCES campuses(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    capacity INTEGER NOT NULL,
    room_type VARCHAR(50),
    facilities_note TEXT,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    
    -- common audit columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    
    -- constraints
    CONSTRAINT uk_rooms_campus_code UNIQUE (campus_id, code),
    CONSTRAINT chk_rooms_capacity CHECK (capacity > 0)
);

-- 3. Create performance indexes
-- Soft delete indexes
CREATE INDEX idx_campuses_deleted_at ON campuses(deleted_at);
CREATE INDEX idx_rooms_deleted_at ON rooms(deleted_at);

-- Foreign key search indexes
CREATE INDEX idx_rooms_campus_id ON rooms(campus_id);

-- Code indexes for direct lookup
CREATE INDEX idx_campuses_code ON campuses(code);
CREATE INDEX idx_rooms_code ON rooms(code);
