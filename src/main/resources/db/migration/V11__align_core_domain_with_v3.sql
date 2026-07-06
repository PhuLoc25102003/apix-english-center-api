-- Forward-only alignment with APIX Backend Standard V3.
-- Existing tables and data are preserved; superseded shapes are extended or deprecated in-place.

-- Employees are required as the operational identity used by class, approval and payroll workflows.
CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    employee_code VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    employment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_employee_status CHECK (employment_status IN ('ACTIVE','INACTIVE','ON_LEAVE','TERMINATED'))
);

CREATE TABLE class_staff (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    staff_role VARCHAR(30) NOT NULL, start_date DATE NOT NULL, end_date DATE,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_class_staff_role CHECK (staff_role IN ('TEACHER','TEACHING_ASSISTANT')),
    CONSTRAINT chk_class_staff_dates CHECK (end_date IS NULL OR start_date <= end_date),
    CONSTRAINT uq_class_staff_assignment UNIQUE (class_id, employee_id, staff_role, start_date)
);

CREATE TABLE class_schedule_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    pattern_type VARCHAR(30) NOT NULL, slot_code VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL, end_date DATE, status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_schedule_pattern CHECK (pattern_type IN ('MWF','TTS','WEEKEND')),
    CONSTRAINT chk_schedule_slot CHECK ((pattern_type IN ('MWF','TTS') AND slot_code IN ('EVENING_1','EVENING_2')) OR (pattern_type='WEEKEND' AND slot_code IN ('MORNING','AFTERNOON'))),
    CONSTRAINT chk_schedule_dates CHECK (end_date IS NULL OR start_date <= end_date),
    CONSTRAINT chk_schedule_status CHECK (status IN ('ACTIVE','INACTIVE'))
);

CREATE TABLE student_learning_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), student_id UUID NOT NULL REFERENCES students(id) ON DELETE RESTRICT,
    class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    note_type VARCHAR(30) NOT NULL DEFAULT 'LEARNING', content TEXT NOT NULL,
    recorded_by UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    handover_to_class_id UUID REFERENCES classes(id) ON DELETE SET NULL,
    is_private BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_learning_note_type CHECK (note_type IN ('LEARNING','BEHAVIOR','HANDOVER'))
);

ALTER TABLE student_attendance ADD COLUMN source VARCHAR(30) NOT NULL DEFAULT 'TEACHER';
ALTER TABLE student_attendance ADD COLUMN excuse_status VARCHAR(30);
ALTER TABLE student_attendance ADD COLUMN parent_notified_at TIMESTAMPTZ;
ALTER TABLE student_attendance ADD COLUMN tuition_credit_eligible BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE student_attendance ADD COLUMN locked_by_office BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE student_attendance ADD CONSTRAINT chk_attendance_source CHECK (source IN ('TEACHER','OFFICE_STAFF','SYSTEM'));
ALTER TABLE student_attendance ADD CONSTRAINT chk_attendance_excuse CHECK (excuse_status IS NULL OR excuse_status IN ('PENDING','APPROVED','REJECTED'));

CREATE TABLE class_session_attendance_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), session_id UUID NOT NULL UNIQUE REFERENCES class_sessions(id) ON DELETE RESTRICT,
    status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED', due_at TIMESTAMPTZ NOT NULL,
    submitted_by UUID REFERENCES employees(id) ON DELETE SET NULL, submitted_at TIMESTAMPTZ,
    reviewed_by UUID REFERENCES employees(id) ON DELETE SET NULL, reviewed_at TIMESTAMPTZ,
    overdue_notified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_attendance_workflow_status CHECK (status IN ('NOT_STARTED','IN_PROGRESS','SUBMITTED','REVIEWED','OVERDUE'))
);

CREATE TABLE file_objects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), bucket VARCHAR(150) NOT NULL, object_key VARCHAR(500) NOT NULL UNIQUE,
    original_filename VARCHAR(255) NOT NULL, content_type VARCHAR(150) NOT NULL, size_bytes BIGINT NOT NULL,
    storage_provider VARCHAR(30) NOT NULL, public_url TEXT, is_public BOOLEAN NOT NULL DEFAULT FALSE,
    checksum VARCHAR(255), uploaded_by UUID REFERENCES employees(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_file_size CHECK (size_bytes >= 0),
    CONSTRAINT chk_storage_provider CHECK (storage_provider IN ('S3','R2','MINIO'))
);

CREATE TABLE weekly_class_updates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    week_start_date DATE NOT NULL, week_end_date DATE NOT NULL, homework_text TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', prepared_by UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    approved_by UUID REFERENCES employees(id) ON DELETE SET NULL, approved_at TIMESTAMPTZ, rejection_reason TEXT,
    delivered_by UUID REFERENCES employees(id) ON DELETE SET NULL, delivered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_weekly_update_dates CHECK (week_start_date <= week_end_date),
    CONSTRAINT chk_weekly_update_status CHECK (status IN ('DRAFT','SUBMITTED','APPROVED','REJECTED','DELIVERED')),
    CONSTRAINT uq_weekly_update_class_week UNIQUE (class_id, week_start_date)
);
CREATE TABLE weekly_update_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), weekly_update_id UUID NOT NULL REFERENCES weekly_class_updates(id) ON DELETE RESTRICT,
    session_date DATE NOT NULL, title VARCHAR(255), content TEXT NOT NULL, display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE weekly_update_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), weekly_update_id UUID NOT NULL REFERENCES weekly_class_updates(id) ON DELETE RESTRICT,
    file_id UUID NOT NULL REFERENCES file_objects(id) ON DELETE RESTRICT, display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_weekly_update_file UNIQUE (weekly_update_id, file_id)
);

CREATE TABLE score_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL, max_score NUMERIC(8,2) NOT NULL, score_date DATE NOT NULL, note TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', created_by_employee UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    delivered_by UUID REFERENCES employees(id) ON DELETE SET NULL, delivered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_score_item_max CHECK (max_score > 0),
    CONSTRAINT chk_score_item_status CHECK (status IN ('DRAFT','PUBLISHED','DELIVERED'))
);
CREATE TABLE score_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), score_item_id UUID NOT NULL REFERENCES score_items(id) ON DELETE RESTRICT,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE RESTRICT, score NUMERIC(8,2) NOT NULL,
    comment TEXT, graded_by UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT, graded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_score_nonnegative CHECK (score >= 0), CONSTRAINT uq_score_item_student UNIQUE (score_item_id, student_id)
);

CREATE TABLE learning_report_cycles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL, period_start DATE NOT NULL, period_end DATE NOT NULL, due_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_report_cycle_dates CHECK (period_start <= period_end AND period_end <= due_date),
    CONSTRAINT chk_report_cycle_status CHECK (status IN ('OPEN','CLOSED'))
);
CREATE TABLE student_learning_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), cycle_id UUID REFERENCES learning_report_cycles(id) ON DELETE SET NULL,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE RESTRICT, class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    report_type VARCHAR(30) NOT NULL, period_start DATE NOT NULL, period_end DATE NOT NULL,
    learning_summary TEXT NOT NULL, attitude_summary TEXT, improvement_notes TEXT, recommendation TEXT, internal_note TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', prepared_by UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    approved_by UUID REFERENCES employees(id) ON DELETE SET NULL, approved_at TIMESTAMPTZ, rejection_reason TEXT,
    delivered_by UUID REFERENCES employees(id) ON DELETE SET NULL, delivered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_learning_report_type CHECK (report_type IN ('PERIODIC','FINAL','CUSTOM')),
    CONSTRAINT chk_learning_report_dates CHECK (period_start <= period_end),
    CONSTRAINT chk_learning_report_status CHECK (status IN ('DRAFT','SUBMITTED','APPROVED','REJECTED','DELIVERED'))
);

CREATE TABLE invoice_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    fee_type VARCHAR(30) NOT NULL, description VARCHAR(255) NOT NULL, quantity NUMERIC(10,2) NOT NULL DEFAULT 1,
    unit_price NUMERIC(15,2) NOT NULL, amount NUMERIC(15,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_invoice_item_type CHECK (fee_type IN ('MONTHLY_TUITION','BOOK','MATERIAL','UNIFORM','OTHER')),
    CONSTRAINT chk_invoice_item_amount CHECK (quantity > 0 AND unit_price >= 0 AND amount >= 0)
);
CREATE TABLE tuition_adjustments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), student_id UUID NOT NULL REFERENCES students(id) ON DELETE RESTRICT,
    invoice_id UUID REFERENCES invoices(id) ON DELETE RESTRICT, attendance_id UUID REFERENCES student_attendance(id) ON DELETE SET NULL,
    adjustment_type VARCHAR(30) NOT NULL, amount NUMERIC(15,2) NOT NULL, reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', approved_by UUID REFERENCES employees(id) ON DELETE SET NULL, approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_adjustment_type CHECK (adjustment_type IN ('CREDIT','REFUND','MANUAL')),
    CONSTRAINT chk_adjustment_amount CHECK (amount > 0),
    CONSTRAINT chk_adjustment_status CHECK (status IN ('PENDING','APPROVED','REJECTED','APPLIED'))
);
CREATE TABLE refund_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), student_id UUID NOT NULL REFERENCES students(id) ON DELETE RESTRICT,
    invoice_id UUID REFERENCES invoices(id) ON DELETE RESTRICT, payment_id UUID REFERENCES invoice_payments(id) ON DELETE RESTRICT,
    amount NUMERIC(15,2) NOT NULL, reason TEXT NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    approved_by UUID REFERENCES employees(id) ON DELETE SET NULL, approved_at TIMESTAMPTZ, refunded_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_refund_amount CHECK (amount > 0), CONSTRAINT chk_refund_status CHECK (status IN ('PENDING','APPROVED','REJECTED','REFUNDED'))
);

CREATE TABLE salary_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    salary_type VARCHAR(30) NOT NULL, hourly_rate NUMERIC(14,2) NOT NULL DEFAULT 0, monthly_base_salary NUMERIC(14,2) NOT NULL DEFAULT 0,
    standard_work_days INTEGER NOT NULL DEFAULT 26, deduct_approved_leave BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL, effective_to DATE, status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_salary_type CHECK (salary_type IN ('HOURLY','MONTHLY')),
    CONSTRAINT chk_salary_values CHECK (hourly_rate >= 0 AND monthly_base_salary >= 0 AND standard_work_days > 0),
    CONSTRAINT chk_salary_dates CHECK (effective_to IS NULL OR effective_from <= effective_to),
    CONSTRAINT chk_salary_status CHECK (status IN ('ACTIVE','INACTIVE'))
);
CREATE TABLE allowance_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), code VARCHAR(100) UNIQUE NOT NULL, name VARCHAR(150) NOT NULL,
    allowance_category VARCHAR(50) NOT NULL, calculation_type VARCHAR(50) NOT NULL, default_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    is_taxable BOOLEAN NOT NULL DEFAULT TRUE, is_active BOOLEAN NOT NULL DEFAULT TRUE, description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE employee_allowances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    allowance_type_id UUID NOT NULL REFERENCES allowance_types(id) ON DELETE RESTRICT, amount NUMERIC(14,2) NOT NULL,
    start_date DATE NOT NULL, end_date DATE, status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE employee_work_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    work_date DATE NOT NULL, source_type VARCHAR(50) NOT NULL, source_id UUID, start_time TIME, end_time TIME,
    work_hours NUMERIC(6,2) NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    approved_by UUID REFERENCES employees(id) ON DELETE SET NULL, approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE leave_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    leave_type VARCHAR(50) NOT NULL, start_date DATE NOT NULL, end_date DATE NOT NULL, total_leave_days NUMERIC(6,2) NOT NULL,
    reason TEXT NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED', approved_by UUID REFERENCES employees(id) ON DELETE SET NULL,
    approved_at TIMESTAMPTZ, rejection_reason TEXT, cancel_requested_at TIMESTAMPTZ,
    cancel_approved_by UUID REFERENCES employees(id) ON DELETE SET NULL, cancel_approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_leave_dates CHECK (start_date <= end_date AND total_leave_days > 0),
    CONSTRAINT chk_leave_status CHECK (status IN ('REQUESTED','APPROVED','REJECTED','CANCEL_REQUESTED','CANCELLED'))
);
CREATE TABLE payroll_periods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), period_code VARCHAR(50) UNIQUE NOT NULL, start_date DATE NOT NULL, end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN', calculated_at TIMESTAMPTZ,
    approved_by UUID REFERENCES employees(id) ON DELETE SET NULL, approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_payroll_period_dates CHECK (start_date <= end_date),
    CONSTRAINT chk_payroll_period_status CHECK (status IN ('OPEN','CALCULATED','APPROVED','PAID','CLOSED'))
);
CREATE TABLE payroll_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), payroll_period_id UUID NOT NULL REFERENCES payroll_periods(id) ON DELETE RESTRICT,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT, salary_type VARCHAR(30) NOT NULL,
    base_amount NUMERIC(14,2) NOT NULL DEFAULT 0, allowance_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    work_amount NUMERIC(14,2) NOT NULL DEFAULT 0, leave_deduction_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    other_deduction_amount NUMERIC(14,2) NOT NULL DEFAULT 0, gross_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    net_amount NUMERIC(14,2) NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0, CONSTRAINT uq_payroll_period_employee UNIQUE (payroll_period_id,employee_id)
);
CREATE TABLE payroll_item_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), payroll_item_id UUID NOT NULL REFERENCES payroll_items(id) ON DELETE RESTRICT,
    line_type VARCHAR(50) NOT NULL, source_type VARCHAR(50) NOT NULL, source_id UUID, description TEXT NOT NULL,
    quantity NUMERIC(10,2), unit_amount NUMERIC(14,2), amount NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE media_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), media_type VARCHAR(30) NOT NULL, video_category VARCHAR(50),
    title VARCHAR(255) NOT NULL, description TEXT, file_id UUID NOT NULL REFERENCES file_objects(id) ON DELETE RESTRICT,
    thumbnail_file_id UUID REFERENCES file_objects(id) ON DELETE SET NULL, class_id UUID REFERENCES classes(id) ON DELETE SET NULL,
    student_id UUID REFERENCES students(id) ON DELETE SET NULL, session_id UUID REFERENCES class_sessions(id) ON DELETE SET NULL,
    media_month DATE, uploaded_by UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT, status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_media_type CHECK (media_type IN ('IMAGE','VIDEO','DOCUMENT')),
    CONSTRAINT chk_video_category CHECK (video_category IS NULL OR video_category IN ('FINAL_COURSE_VIDEO','FOREIGN_TEACHER_ACTIVITY','MONTHLY_PERSONAL_VIDEO','OTHER')),
    CONSTRAINT chk_media_status CHECK (status IN ('DRAFT','READY','PUBLISHED','ARCHIVED'))
);
CREATE TABLE media_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), media_item_id UUID NOT NULL REFERENCES media_items(id) ON DELETE RESTRICT,
    parent_id UUID REFERENCES parents(id) ON DELETE SET NULL, student_id UUID REFERENCES students(id) ON DELETE SET NULL,
    delivered_by UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT, channel VARCHAR(30) NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    delivered_at TIMESTAMPTZ, read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), code VARCHAR(100) UNIQUE NOT NULL, channel VARCHAR(30) NOT NULL,
    title_template TEXT NOT NULL, body_template TEXT NOT NULL, is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), template_id UUID REFERENCES notification_templates(id) ON DELETE SET NULL,
    title TEXT NOT NULL, body TEXT NOT NULL, target_type VARCHAR(30) NOT NULL, target_user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    channel VARCHAR(30) NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'PENDING', scheduled_at TIMESTAMPTZ, sent_at TIMESTAMPTZ,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), actor_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    actor_employee_id UUID REFERENCES employees(id) ON DELETE SET NULL, action VARCHAR(100) NOT NULL, module VARCHAR(100) NOT NULL,
    entity_type VARCHAR(150) NOT NULL, entity_id UUID, before_data JSONB, after_data JSONB,
    ip_address VARCHAR(100), user_agent TEXT, request_id VARCHAR(100), created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Permission codes are the only business authorization contract. Role names remain seed templates only.
INSERT INTO roles (code,name,description,is_system,is_active)
VALUES ('SUPER_ADMIN','Super Admin','Technical administrator with every permission',TRUE,TRUE),
       ('CENTER_MANAGER','Center Manager','Center operational manager',TRUE,TRUE),
       ('HR_ACCOUNTANT','HR / Accountant','Payroll and finance operator',TRUE,TRUE),
       ('ACADEMIC_MANAGER','Academic Manager','Academic quality manager',TRUE,TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (code,module,action,description,is_active)
SELECT v.code, split_part(v.code,':',1), split_part(v.code,':',2), 'V3 permission ' || v.code, TRUE
FROM (VALUES
 ('class:assign_staff'),('class:view_assigned'),('student_note:read'),('student_note:create'),
 ('schedule:read'),('schedule:manage'),('attendance:read'),('attendance:mark'),('attendance:mark_any'),('attendance:override_office'),('attendance:review'),
 ('weekly_update:read'),('weekly_update:create'),('weekly_update:update'),('weekly_update:submit'),('weekly_update:approve'),('weekly_update:reject'),('weekly_update:deliver'),
 ('score:read'),('score:create'),('score:update'),('score:deliver'),
 ('learning_report:read'),('learning_report:create'),('learning_report:update'),('learning_report:submit'),('learning_report:approve'),('learning_report:reject'),('learning_report:deliver'),
 ('tuition:adjust'),('tuition:refund'),('payroll:read'),('payroll:calculate'),('payroll:approve'),('payroll:mark_paid'),
 ('leave:read'),('leave:create'),('leave:approve'),('leave:cancel'),('media:read'),('media:create'),('media:publish'),('media:deliver'),
 ('notification:read'),('notification:manage'),('audit:read'),('dashboard:owner'),('dashboard:office'),('dashboard:teacher'),('dashboard:parent'),('dashboard:student'),('dashboard:employee')
) AS v(code)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p WHERE r.code IN ('SUPER_ADMIN','OWNER')
ON CONFLICT (role_id,permission_id) DO NOTHING;
INSERT INTO role_permissions (role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p WHERE r.code='OFFICE_STAFF' AND p.code IN
('attendance:read','attendance:mark_any','attendance:review','weekly_update:read','weekly_update:approve','weekly_update:reject','weekly_update:deliver','score:read','score:deliver','learning_report:read','learning_report:approve','learning_report:reject','learning_report:deliver','media:read','media:publish','media:deliver','dashboard:office')
ON CONFLICT (role_id,permission_id) DO NOTHING;
INSERT INTO role_permissions (role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p WHERE r.code='TEACHER' AND p.code IN
('class:view_assigned','student_note:read','student_note:create','attendance:read','attendance:mark','weekly_update:read','weekly_update:create','weekly_update:update','weekly_update:submit','score:read','score:create','score:update','learning_report:read','learning_report:create','learning_report:update','learning_report:submit','media:read','dashboard:teacher')
ON CONFLICT (role_id,permission_id) DO NOTHING;
INSERT INTO role_permissions (role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p WHERE r.code='PARENT' AND p.code IN ('weekly_update:read','score:read','learning_report:read','media:read','dashboard:parent')
ON CONFLICT (role_id,permission_id) DO NOTHING;
INSERT INTO role_permissions (role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p WHERE r.code='STUDENT' AND p.code IN ('weekly_update:read','score:read','learning_report:read','media:read','dashboard:student')
ON CONFLICT (role_id,permission_id) DO NOTHING;

CREATE INDEX idx_class_staff_employee_active ON class_staff(employee_id,class_id,end_date);
CREATE INDEX idx_schedule_class_status ON class_schedule_plans(class_id,status);
CREATE INDEX idx_sessions_date_status ON class_sessions(session_date,status);
CREATE INDEX idx_attendance_session_student ON student_attendance(session_id,student_id);
CREATE INDEX idx_attendance_status_source ON student_attendance(status,source);
CREATE INDEX idx_attendance_credit ON student_attendance(tuition_credit_eligible);
CREATE INDEX idx_attendance_workflow_due ON class_session_attendance_status(status,due_at);
CREATE INDEX idx_weekly_updates_class_week ON weekly_class_updates(class_id,week_start_date,week_end_date);
CREATE INDEX idx_weekly_updates_status ON weekly_class_updates(status);
CREATE INDEX idx_score_items_class_status ON score_items(class_id,status);
CREATE INDEX idx_score_records_student ON score_records(student_id);
CREATE INDEX idx_learning_cycles_class_period ON learning_report_cycles(class_id,period_start,period_end);
CREATE INDEX idx_learning_reports_status ON student_learning_reports(status);
CREATE INDEX idx_invoices_student_status ON invoices(student_id,status);
CREATE INDEX idx_invoices_due_status ON invoices(due_date,status);
CREATE INDEX idx_payroll_items_period_employee ON payroll_items(payroll_period_id,employee_id);
CREATE INDEX idx_leave_requests_employee_status ON leave_requests(employee_id,status);
CREATE INDEX idx_media_items_student_category ON media_items(student_id,video_category);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type,entity_id);
CREATE INDEX idx_audit_logs_actor_date ON audit_logs(actor_user_id,created_at);

COMMENT ON TABLE weekly_class_updates IS 'V3 replacement for student-submitted homework/assignment flow; homework is parent-facing text in a weekly update.';
