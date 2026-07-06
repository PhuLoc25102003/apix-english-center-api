package com.apixenglish.center.modules.media.entity;

import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name="video_upload_sessions") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VideoUploadSession extends SoftDeleteEntity {
 @Column(name="upload_token",nullable=false,unique=true) private String uploadToken;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="class_id") private Clazz clazz;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="student_id") private Student student;
 @Column(name="session_id") private UUID sessionId;
 @Enumerated(EnumType.STRING) @Column(name="video_type",nullable=false) private VideoType videoType;
 @Column(name="target_month") private LocalDate targetMonth;
 @Column private String title; @Column(columnDefinition="text") private String description;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="requested_by",nullable=false) private Employee requestedBy;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private VideoUploadStatus status;
 @Column(name="expires_at",nullable=false) private Instant expiresAt; @Column(name="completed_at") private Instant completedAt;
 @Column(name="cancelled_at") private Instant cancelledAt; @Column(name="failure_reason") private String failureReason;
 @Column(name="expected_storage_bucket") private String expectedStorageBucket; @Column(name="expected_storage_key") private String expectedStorageKey;
 @Column(name="expected_file_name") private String expectedFileName; @Column(name="expected_mime_type") private String expectedMimeType;
 @Column(name="expected_file_size_bytes") private Long expectedFileSizeBytes;
}
