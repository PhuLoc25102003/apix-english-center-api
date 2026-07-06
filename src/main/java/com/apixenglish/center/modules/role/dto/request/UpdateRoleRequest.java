package com.apixenglish.center.modules.role.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

    @NotBlank(message = "Role code is required")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Role code must be uppercase letters, numbers, hyphens or underscores")
    private String code;

    @NotBlank(message = "Role name is required")
    private String name;

    private String description;

    private Boolean isActive;
}
