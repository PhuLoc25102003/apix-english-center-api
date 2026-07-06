CREATE TABLE video_upload_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), upload_token VARCHAR(255) UNIQUE NOT NULL,
    class_id UUID REFERENCES classes(id) ON DELETE SET NULL, student_id UUID REFERENCES students(id) ON DELETE SET NULL,
    session_id UUID REFERENCES class_sessions(id) ON DELETE SET NULL, video_type VARCHAR(50) NOT NULL,
    target_month DATE, title VARCHAR(255), description TEXT, requested_by UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', expires_at TIMESTAMPTZ NOT NULL, completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ, failure_reason TEXT,
    expected_storage_bucket VARCHAR(150), expected_storage_key TEXT, expected_file_name VARCHAR(255),
    expected_mime_type VARCHAR(150), expected_file_size_bytes BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_video_upload_type CHECK (video_type IN ('FINAL_COURSE_VIDEO','FOREIGN_TEACHER_ACTIVITY','MONTHLY_PERSONAL_VIDEO','CLASS_ACTIVITY_VIDEO','CUSTOM')),
    CONSTRAINT chk_video_upload_status CHECK (status IN ('PENDING','UPLOADING','COMPLETED','EXPIRED','CANCELLED','FAILED')),
    CONSTRAINT chk_video_upload_expiry CHECK (expires_at > created_at),
    CONSTRAINT chk_video_upload_target CHECK (student_id IS NOT NULL OR class_id IS NOT NULL),
    CONSTRAINT chk_video_upload_monthly CHECK (video_type <> 'MONTHLY_PERSONAL_VIDEO' OR (student_id IS NOT NULL AND target_month IS NOT NULL AND EXTRACT(DAY FROM target_month) = 1)),
    CONSTRAINT chk_video_upload_class CHECK (video_type NOT IN ('CLASS_ACTIVITY_VIDEO','FOREIGN_TEACHER_ACTIVITY') OR class_id IS NOT NULL)
);

CREATE TABLE media_videos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), upload_session_id UUID UNIQUE REFERENCES video_upload_sessions(id) ON DELETE SET NULL,
    class_id UUID REFERENCES classes(id) ON DELETE SET NULL, student_id UUID REFERENCES students(id) ON DELETE SET NULL,
    session_id UUID REFERENCES class_sessions(id) ON DELETE SET NULL, video_type VARCHAR(50) NOT NULL, target_month DATE,
    title VARCHAR(255) NOT NULL, description TEXT, original_file_name VARCHAR(255) NOT NULL,
    storage_bucket VARCHAR(150) NOT NULL, storage_key TEXT NOT NULL UNIQUE, mime_type VARCHAR(150) NOT NULL,
    file_size_bytes BIGINT NOT NULL, duration_seconds INTEGER, checksum VARCHAR(255), status VARCHAR(30) NOT NULL,
    uploaded_by UUID REFERENCES employees(id) ON DELETE SET NULL, approved_by UUID REFERENCES employees(id) ON DELETE SET NULL,
    approved_at TIMESTAMPTZ, rejected_by UUID REFERENCES employees(id) ON DELETE SET NULL, rejected_at TIMESTAMPTZ,
    rejection_reason TEXT, delivered_at TIMESTAMPTZ, parent_visible BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_media_video_type CHECK (video_type IN ('FINAL_COURSE_VIDEO','FOREIGN_TEACHER_ACTIVITY','MONTHLY_PERSONAL_VIDEO','CLASS_ACTIVITY_VIDEO','CUSTOM')),
    CONSTRAINT chk_media_video_status CHECK (status IN ('UPLOADED','PROCESSING','READY','APPROVED','REJECTED','DELIVERED','ARCHIVED')),
    CONSTRAINT chk_media_video_size CHECK (file_size_bytes > 0),
    CONSTRAINT chk_media_video_duration CHECK (duration_seconds IS NULL OR duration_seconds >= 0)
);

CREATE TABLE video_share_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), video_id UUID NOT NULL REFERENCES media_videos(id) ON DELETE RESTRICT,
    share_token VARCHAR(255) UNIQUE NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ, created_for_parent_id UUID REFERENCES parents(id) ON DELETE SET NULL,
    created_by_employee_id UUID REFERENCES employees(id) ON DELETE SET NULL, access_count INTEGER NOT NULL DEFAULT 0,
    last_accessed_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_video_share_status CHECK (status IN ('ACTIVE','EXPIRED','REVOKED')),
    CONSTRAINT chk_video_share_access_count CHECK (access_count >= 0)
);

CREATE TABLE notification_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), entity_type VARCHAR(50) NOT NULL, entity_id UUID NOT NULL,
    parent_id UUID REFERENCES parents(id) ON DELETE SET NULL, student_id UUID REFERENCES students(id) ON DELETE SET NULL,
    channel VARCHAR(30) NOT NULL, recipient_name VARCHAR(255), recipient_phone VARCHAR(30), message_content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PREPARED', prepared_by UUID REFERENCES employees(id) ON DELETE SET NULL,
    sent_by UUID REFERENCES employees(id) ON DELETE SET NULL, sent_at TIMESTAMPTZ, note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ, version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_notification_delivery_channel CHECK (channel = 'MANUAL_ZALO'),
    CONSTRAINT chk_notification_delivery_status CHECK (status IN ('PREPARED','COPIED','OPENED_ZALO','SENT_MANUALLY','FAILED'))
);

CREATE INDEX idx_video_upload_expires_status ON video_upload_sessions(expires_at,status);
CREATE INDEX idx_media_videos_student ON media_videos(student_id);
CREATE INDEX idx_media_videos_class ON media_videos(class_id);
CREATE INDEX idx_media_videos_month ON media_videos(target_month);
CREATE INDEX idx_media_videos_type ON media_videos(video_type);
CREATE INDEX idx_media_videos_status ON media_videos(status);
CREATE INDEX idx_media_videos_uploaded_by ON media_videos(uploaded_by);
CREATE INDEX idx_video_share_video_status ON video_share_links(video_id,status);
CREATE INDEX idx_notification_delivery_entity ON notification_deliveries(entity_type,entity_id);

INSERT INTO permissions(code,module,action,description,is_active) VALUES
('media-video:read','media-video','read','Read media videos',TRUE),
('media-video:create-upload-session','media-video','create-upload-session','Create QR video upload sessions',TRUE),
('media-video:upload','media-video','upload','Upload video through a scoped session',TRUE),
('media-video:approve','media-video','approve','Approve media videos',TRUE),
('media-video:reject','media-video','reject','Reject media videos',TRUE),
('media-video:create-share-link','media-video','create-share-link','Create parent video share links',TRUE),
('media-video:revoke-share-link','media-video','revoke-share-link','Revoke parent video share links',TRUE),
('media-video:deliver','media-video','deliver','Deliver media videos',TRUE),
('media-video:delete','media-video','delete','Soft-delete media videos',TRUE),
('media-video:archive','media-video','archive','Archive media videos',TRUE),
('manual-zalo:prepare','manual-zalo','prepare','Prepare Manual Zalo delivery',TRUE),
('manual-zalo:update-status','manual-zalo','update-status','Update Manual Zalo delivery status',TRUE)
ON CONFLICT(code) DO UPDATE SET description=EXCLUDED.description,is_active=TRUE;

INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p WHERE r.code IN ('SUPER_ADMIN','OWNER') AND (p.module='media-video' OR p.module='manual-zalo')
ON CONFLICT(role_id,permission_id) DO NOTHING;
INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p WHERE r.code IN ('CENTER_MANAGER','OFFICE_STAFF') AND p.code IN
('media-video:read','media-video:create-upload-session','media-video:upload','media-video:approve','media-video:reject','media-video:create-share-link','media-video:revoke-share-link','media-video:deliver','media-video:archive','manual-zalo:prepare','manual-zalo:update-status')
ON CONFLICT(role_id,permission_id) DO NOTHING;
INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p WHERE r.code='TEACHER' AND p.code IN
('media-video:read','media-video:create-upload-session','media-video:upload')
ON CONFLICT(role_id,permission_id) DO NOTHING;
