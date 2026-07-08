package com.apixenglish.center.modules.curriculum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateCurriculumRequest {
    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Version name is required")
    private String versionName;

    private String description;

    @NotNull(message = "isActive is required")
    private Boolean isActive;
}
