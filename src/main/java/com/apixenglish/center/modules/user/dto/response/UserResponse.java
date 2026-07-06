package com.apixenglish.center.modules.user.dto.response;

import com.apixenglish.center.modules.user.entity.UserStatus;
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
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String username;
    private String avatarUrl;
    private UserStatus status;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean mfaEnabled;
    private Instant lastLoginAt;
    private List<RoleSummary> roles;
    private List<CampusSummary> campusScopes;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampusSummary {
        private UUID id;
        private String code;
        private String name;
    }
}
