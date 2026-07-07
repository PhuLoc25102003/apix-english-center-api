CREATE TABLE video_delivery_batches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID REFERENCES classes(id) ON DELETE SET NULL,
    video_type VARCHAR(50) NOT NULL,
    target_month DATE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_by_employee_id UUID REFERENCES employees(id) ON DELETE SET NULL,
    completed_at TIMESTAMPTZ,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_video_delivery_batch_type CHECK (video_type IN ('MONTHLY_PERSONAL_VIDEO','FINAL_COURSE_VIDEO','FOREIGN_TEACHER_ACTIVITY','CLASS_ACTIVITY_VIDEO','CUSTOM')),
    CONSTRAINT chk_video_delivery_batch_status CHECK (status IN ('DRAFT','ACTIVE','COMPLETED','CANCELLED')),
    CONSTRAINT chk_video_delivery_batch_month CHECK (target_month IS NULL OR EXTRACT(DAY FROM target_month)=1),
    CONSTRAINT chk_monthly_delivery_batch CHECK (video_type <> 'MONTHLY_PERSONAL_VIDEO' OR target_month IS NOT NULL)
);

CREATE TABLE student_video_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID REFERENCES video_delivery_batches(id) ON DELETE SET NULL,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE RESTRICT,
    class_id UUID REFERENCES classes(id) ON DELETE SET NULL,
    parent_id UUID REFERENCES parents(id) ON DELETE SET NULL,
    video_type VARCHAR(50) NOT NULL,
    target_month DATE,
    title VARCHAR(255) NOT NULL,
    message_content TEXT,
    channel VARCHAR(30) NOT NULL DEFAULT 'MANUAL_ZALO_DIRECT',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    assigned_to_employee_id UUID REFERENCES employees(id) ON DELETE SET NULL,
    sent_by_employee_id UUID REFERENCES employees(id) ON DELETE SET NULL,
    sent_at TIMESTAMPTZ,
    failed_reason TEXT,
    skipped_reason TEXT,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_student_video_delivery_batch UNIQUE(batch_id,student_id),
    CONSTRAINT chk_student_video_delivery_type CHECK (video_type IN ('MONTHLY_PERSONAL_VIDEO','FINAL_COURSE_VIDEO','FOREIGN_TEACHER_ACTIVITY','CLASS_ACTIVITY_VIDEO','CUSTOM')),
    CONSTRAINT chk_student_video_delivery_channel CHECK (channel IN ('MANUAL_ZALO_DIRECT','MANUAL_ZALO_LINK','OTHER')),
    CONSTRAINT chk_student_video_delivery_status CHECK (status IN ('PENDING','PREPARED','OPENED_ZALO','SENT_MANUALLY','FAILED','SKIPPED','CANCELLED')),
    CONSTRAINT chk_student_video_delivery_month CHECK (target_month IS NULL OR EXTRACT(DAY FROM target_month)=1)
);

CREATE TABLE video_delivery_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_delivery_id UUID NOT NULL REFERENCES student_video_deliveries(id) ON DELETE RESTRICT,
    action VARCHAR(30) NOT NULL,
    actor_employee_id UUID REFERENCES employees(id) ON DELETE SET NULL,
    parent_id UUID REFERENCES parents(id) ON DELETE SET NULL,
    recipient_phone VARCHAR(30),
    message_content TEXT,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_video_delivery_attempt_action CHECK (action IN ('PREPARED_MESSAGE','COPIED_MESSAGE','OPENED_ZALO','MARKED_SENT','MARKED_FAILED','MARKED_SKIPPED','REOPENED'))
);

CREATE INDEX idx_video_delivery_batches_class ON video_delivery_batches(class_id);
CREATE INDEX idx_video_delivery_batches_type_month ON video_delivery_batches(video_type,target_month);
CREATE INDEX idx_video_delivery_batches_status_due ON video_delivery_batches(status,due_date);
CREATE INDEX idx_student_video_deliveries_batch_status ON student_video_deliveries(batch_id,status);
CREATE INDEX idx_student_video_deliveries_student ON student_video_deliveries(student_id);
CREATE INDEX idx_student_video_deliveries_class ON student_video_deliveries(class_id);
CREATE INDEX idx_student_video_deliveries_parent ON student_video_deliveries(parent_id);
CREATE INDEX idx_student_video_deliveries_assigned ON student_video_deliveries(assigned_to_employee_id);
CREATE INDEX idx_student_video_deliveries_sent_by ON student_video_deliveries(sent_by_employee_id);
CREATE INDEX idx_video_delivery_attempts_delivery_date ON video_delivery_attempts(video_delivery_id,created_at);

INSERT INTO permissions(code,module,action,description,is_active) VALUES
('video-delivery:read','video-delivery','read','Read video delivery tracking',TRUE),
('video-delivery:create-batch','video-delivery','create-batch','Create video delivery batches',TRUE),
('video-delivery:update-batch','video-delivery','update-batch','Update video delivery batches',TRUE),
('video-delivery:cancel-batch','video-delivery','cancel-batch','Cancel video delivery batches',TRUE),
('video-delivery:prepare-message','video-delivery','prepare-message','Prepare direct Zalo messages',TRUE),
('video-delivery:mark-copied','video-delivery','mark-copied','Record copied Zalo messages',TRUE),
('video-delivery:open-zalo','video-delivery','open-zalo','Record opening parent Zalo',TRUE),
('video-delivery:mark-sent','video-delivery','mark-sent','Mark direct video delivery sent',TRUE),
('video-delivery:mark-failed','video-delivery','mark-failed','Mark video delivery failed',TRUE),
('video-delivery:mark-skipped','video-delivery','mark-skipped','Mark video delivery skipped',TRUE),
('video-delivery:reopen','video-delivery','reopen','Reopen failed or skipped video delivery',TRUE),
('video-delivery:assign','video-delivery','assign','Assign video delivery responsibility',TRUE)
ON CONFLICT(code) DO UPDATE SET description=EXCLUDED.description,is_active=TRUE;

INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p
WHERE r.code IN ('SUPER_ADMIN','OWNER','CENTER_MANAGER') AND p.module='video-delivery'
ON CONFLICT(role_id,permission_id) DO NOTHING;

INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p
WHERE r.code='OFFICE_STAFF' AND p.module='video-delivery'
ON CONFLICT(role_id,permission_id) DO NOTHING;

INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p
WHERE r.code='TEACHER' AND p.code='video-delivery:read'
ON CONFLICT(role_id,permission_id) DO NOTHING;
