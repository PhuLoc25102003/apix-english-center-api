package com.apixenglish.center.modules.audit.repository;
import com.apixenglish.center.modules.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    java.util.List<AuditLog> findTop20ByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);
}
