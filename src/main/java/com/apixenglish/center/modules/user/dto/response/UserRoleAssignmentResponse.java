package com.apixenglish.center.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleAssignmentResponse {
    private UUID userId;
    private List<RoleAssignmentItem> roles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleAssignmentItem {
        private UUID assignmentId;
        private UUID roleId;
        private String roleCode;
        private String roleName;
        private UUID campusId;
        private String campusName;
        private Instant assignedAt;
        private Instant expiredAt;
        private Boolean isActive;
    }
}
