package com.apixenglish.center.modules.position.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePositionRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "isTeachingPosition is required")
    private Boolean isTeachingPosition;

    @NotNull(message = "isActive is required")
    private Boolean isActive;
}
