package com.apixenglish.center.modules.position.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePositionRequest {
    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "Code must be uppercase alphanumeric and start with a letter")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "isTeachingPosition is required")
    private Boolean isTeachingPosition;

    @NotNull(message = "isActive is required")
    private Boolean isActive;
}
