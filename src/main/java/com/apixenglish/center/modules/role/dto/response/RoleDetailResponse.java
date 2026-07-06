package com.apixenglish.center.modules.role.dto.response;

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
public class RoleDetailResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private Boolean isSystem;
    private Boolean isActive;
    private List<PermissionSummary> permissions;
    private Instant createdAt;
    private Instant updatedAt;
}
