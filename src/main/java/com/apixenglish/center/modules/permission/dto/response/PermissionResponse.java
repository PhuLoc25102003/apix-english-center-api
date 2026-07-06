package com.apixenglish.center.modules.permission.dto.response;

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
public class PermissionResponse {
    private UUID id;
    private String code;
    private String module;
    private String action;
    private String description;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
