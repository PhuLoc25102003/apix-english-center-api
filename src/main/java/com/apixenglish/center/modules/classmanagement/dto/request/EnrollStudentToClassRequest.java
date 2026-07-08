package com.apixenglish.center.modules.classmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class EnrollStudentToClassRequest {
    @NotNull(message = "Student ID must not be null")
    private UUID studentId;

    @NotNull(message = "Enrolled date must not be null")
    private LocalDate enrolledDate;

    @NotNull(message = "Start date must not be null")
    private LocalDate startDate;

    private String status; // TRIAL, ACTIVE, FROZEN

    private String source; // WALK_IN, REFERRAL, ONLINE, OTHER

    private String note;
}
