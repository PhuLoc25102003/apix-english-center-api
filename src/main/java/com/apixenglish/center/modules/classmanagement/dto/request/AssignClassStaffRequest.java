package com.apixenglish.center.modules.classmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AssignClassStaffRequest {
    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotBlank(message = "Staff type is required")
    private String staffType; // TEACHER, TEACHING_ASSISTANT

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "isPrimary is required")
    private Boolean isPrimary;
}
