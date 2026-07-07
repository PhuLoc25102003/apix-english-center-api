package com.apixenglish.center.modules.videodelivery.entity;
import com.apixenglish.center.modules.employee.entity.Employee; import com.apixenglish.center.modules.parent.entity.Parent;
import jakarta.persistence.*; import lombok.*; import org.springframework.data.annotation.CreatedBy; import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant; import java.util.UUID;
@Entity @Table(name="video_delivery_attempts") @EntityListeners(AuditingEntityListener.class) @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VideoDeliveryAttempt {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="video_delivery_id",nullable=false) private StudentVideoDelivery videoDelivery;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private VideoAttemptAction action;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="actor_employee_id") private Employee actorEmployee;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="parent_id") private Parent parent;
 @Column(name="recipient_phone") private String recipientPhone; @Column(name="message_content",columnDefinition="text") private String messageContent;
 @Column(columnDefinition="text") private String note; @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @CreatedBy @Column(name="created_by",updatable=false) private UUID createdBy;
 @PrePersist void create(){if(createdAt==null)createdAt=Instant.now();}
}
