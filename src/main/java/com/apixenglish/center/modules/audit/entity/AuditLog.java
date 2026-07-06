package com.apixenglish.center.modules.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name="actor_user_id") private UUID actorUserId;
    @Column(name="actor_employee_id") private UUID actorEmployeeId;
    @Column(nullable=false) private String action;
    @Column(nullable=false) private String module;
    @Column(name="entity_type", nullable=false) private String entityType;
    @Column(name="entity_id") private UUID entityId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name="before_data", columnDefinition="jsonb") private Map<String,Object> beforeData;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name="after_data", columnDefinition="jsonb") private Map<String,Object> afterData;
    @Column(name="ip_address") private String ipAddress;
    @Column(name="user_agent") private String userAgent;
    @Column(name="request_id") private String requestId;
    @Column(name="created_at", nullable=false) private Instant createdAt;
}
