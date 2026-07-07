package com.apixenglish.center.modules.videodelivery.entity;
import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.media.entity.VideoType;
import jakarta.persistence.*; import lombok.*; import java.time.*;
@Entity @Table(name="video_delivery_batches") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VideoDeliveryBatch extends SoftDeleteEntity {
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="class_id") private Clazz clazz;
 @Enumerated(EnumType.STRING) @Column(name="video_type",nullable=false) private VideoType videoType;
 @Column(name="target_month") private LocalDate targetMonth; @Column(nullable=false) private String title;
 @Column(columnDefinition="text") private String description; @Column(name="due_date") private LocalDate dueDate;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private VideoBatchStatus status;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by_employee_id") private Employee createdByEmployee;
 @Column(name="completed_at") private Instant completedAt; @Column(columnDefinition="text") private String note;
}
