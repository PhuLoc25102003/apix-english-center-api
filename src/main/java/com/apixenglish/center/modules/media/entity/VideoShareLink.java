package com.apixenglish.center.modules.media.entity;
import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.parent.entity.Parent;
import jakarta.persistence.*; import lombok.*; import java.time.*;
@Entity @Table(name="video_share_links") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VideoShareLink extends SoftDeleteEntity {
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="video_id",nullable=false) private MediaVideo video;
 @Column(name="share_token",nullable=false,unique=true) private String shareToken;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private ShareLinkStatus status;
 @Column(name="expires_at") private Instant expiresAt; @Column(name="revoked_at") private Instant revokedAt;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_for_parent_id") private Parent createdForParent;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by_employee_id") private Employee createdByEmployee;
 @Column(name="access_count",nullable=false) private Integer accessCount; @Column(name="last_accessed_at") private Instant lastAccessedAt;
}
