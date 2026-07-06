package com.apixenglish.center.modules.role.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private Boolean isSystem;
    private Boolean isActive;
    private Long permissionCount;
    private Instant createdAt;
    private Instant updatedAt;
}
