package com.apixenglish.center.modules.media.entity;
import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.employee.entity.Employee; import com.apixenglish.center.modules.parent.entity.Parent;
import com.apixenglish.center.modules.student.entity.Student; import jakarta.persistence.*; import lombok.*; import java.time.*; import java.util.UUID;
@Entity @Table(name="notification_deliveries") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationDelivery extends SoftDeleteEntity {
 @Column(name="entity_type",nullable=false) private String entityType; @Column(name="entity_id",nullable=false) private UUID entityId;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="parent_id") private Parent parent;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="student_id") private Student student;
 @Column(nullable=false) private String channel; @Column(name="recipient_name") private String recipientName;
 @Column(name="recipient_phone") private String recipientPhone; @Column(name="message_content",nullable=false,columnDefinition="text") private String messageContent;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private DeliveryStatus status;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="prepared_by") private Employee preparedBy;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sent_by") private Employee sentBy; @Column(name="sent_at") private Instant sentAt;
 @Column(columnDefinition="text") private String note;
}
