package com.apixenglish.center.modules.media.entity;

import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name="media_videos") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MediaVideo extends SoftDeleteEntity {
 @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="upload_session_id") private VideoUploadSession uploadSession;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="class_id") private Clazz clazz;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="student_id") private Student student;
 @Column(name="session_id") private UUID sessionId;
 @Enumerated(EnumType.STRING) @Column(name="video_type",nullable=false) private VideoType videoType;
 @Column(name="target_month") private LocalDate targetMonth; @Column(nullable=false) private String title;
 @Column(columnDefinition="text") private String description; @Column(name="original_file_name",nullable=false) private String originalFileName;
 @Column(name="storage_bucket",nullable=false) private String storageBucket; @Column(name="storage_key",nullable=false) private String storageKey;
 @Column(name="mime_type",nullable=false) private String mimeType; @Column(name="file_size_bytes",nullable=false) private Long fileSizeBytes;
 @Column(name="duration_seconds") private Integer durationSeconds; @Column private String checksum;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private MediaVideoStatus status;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="uploaded_by") private Employee uploadedBy;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="approved_by") private Employee approvedBy; @Column(name="approved_at") private Instant approvedAt;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="rejected_by") private Employee rejectedBy; @Column(name="rejected_at") private Instant rejectedAt;
 @Column(name="rejection_reason") private String rejectionReason; @Column(name="delivered_at") private Instant deliveredAt;
 @Column(name="parent_visible",nullable=false) private Boolean parentVisible;
}
