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
public class RoleLookupResponse {
    private UUID id;
    private String code;
    private String name;
    private String displayName;
    private Boolean isSystem;
    private Boolean isActive;
}
