package com.apixenglish.center.modules.permission.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePermissionRequest {

    @NotBlank(message = "Permission code is required")
    private String code;

    @NotBlank(message = "Module is required")
    private String module;

    @NotBlank(message = "Action is required")
    private String action;

    private String description;

    private Boolean isActive;
}
