package com.apixenglish.center.modules.role.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionSummary {
    private UUID id;
    private String code;
    private String module;
    private String action;
    private String description;
    private Boolean isActive;
}
