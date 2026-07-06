package com.apixenglish.center.modules.audit.service;

import com.apixenglish.center.modules.audit.entity.AuditLog;
import com.apixenglish.center.modules.audit.repository.AuditLogRepository;
import com.apixenglish.center.modules.employee.repository.EmployeeRepository;
import com.apixenglish.center.security.CurrentActor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repository;
    private final CurrentActor currentActor;
    private final EmployeeRepository employeeRepository;
    private final ObjectProvider<HttpServletRequest> requestProvider;

    public void record(String action, String module, String entityType, UUID entityId,
                       Map<String,Object> before, Map<String,Object> after) {
        UUID userId = currentActor.userId();
        recordAs(userId, employeeRepository.findByUserIdAndDeletedAtIsNull(userId).map(e -> e.getId()).orElse(null),
                action, module, entityType, entityId, before, after);
    }

    public void recordAs(UUID actorUserId, UUID actorEmployeeId, String action, String module,
                         String entityType, UUID entityId, Map<String,Object> before, Map<String,Object> after) {
        HttpServletRequest request = requestProvider.getIfAvailable();
        repository.save(AuditLog.builder().actorUserId(actorUserId)
                .actorEmployeeId(actorEmployeeId)
                .action(action).module(module).entityType(entityType).entityId(entityId)
                .beforeData(before).afterData(after)
                .ipAddress(request == null ? null : request.getRemoteAddr())
                .userAgent(request == null ? null : request.getHeader("User-Agent"))
                .requestId(request == null ? null : request.getHeader("X-Request-Id"))
                .createdAt(Instant.now()).build());
    }
}
