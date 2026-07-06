package com.apixenglish.center.modules.permission.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionLookupResponse {
    private UUID id;
    private String code;
    private String module;
    private String action;
    private String displayName;
    private Boolean isActive;
}
