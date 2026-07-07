package com.apixenglish.center.modules.videodelivery.entity;
import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.classmanagement.entity.Clazz; import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.media.entity.VideoType; import com.apixenglish.center.modules.parent.entity.Parent; import com.apixenglish.center.modules.student.entity.Student;
import jakarta.persistence.*; import lombok.*; import java.time.*;
@Entity @Table(name="student_video_deliveries") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentVideoDelivery extends SoftDeleteEntity {
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="batch_id") private VideoDeliveryBatch batch;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="student_id",nullable=false) private Student student;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="class_id") private Clazz clazz;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="parent_id") private Parent parent;
 @Enumerated(EnumType.STRING) @Column(name="video_type",nullable=false) private VideoType videoType;
 @Column(name="target_month") private LocalDate targetMonth; @Column(nullable=false) private String title;
 @Column(name="message_content",columnDefinition="text") private String messageContent;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private VideoDeliveryChannel channel;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private VideoDeliveryStatus status;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assigned_to_employee_id") private Employee assignedToEmployee;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sent_by_employee_id") private Employee sentByEmployee;
 @Column(name="sent_at") private Instant sentAt; @Column(name="failed_reason",columnDefinition="text") private String failedReason;
 @Column(name="skipped_reason",columnDefinition="text") private String skippedReason; @Column(columnDefinition="text") private String note;
}
