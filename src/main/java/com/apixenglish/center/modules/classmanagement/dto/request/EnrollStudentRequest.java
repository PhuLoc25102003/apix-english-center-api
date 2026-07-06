package com.apixenglish.center.modules.classmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollStudentRequest {

    @NotNull(message = "Student ID must not be null")
    private UUID studentId;

    @NotNull(message = "Class ID must not be null")
    private UUID classId;

    @NotNull(message = "Enrolled date must not be null")
    private LocalDate enrolledDate;

    @NotNull(message = "Start date must not be null")
    private LocalDate startDate;

    @jakarta.validation.constraints.Pattern(regexp = "TRIAL|ACTIVE|FROZEN", message = "Status must be TRIAL, ACTIVE, or FROZEN")
    private String status;

    @jakarta.validation.constraints.Pattern(regexp = "WALK_IN|REFERRAL|ONLINE|OTHER", message = "Unsupported enrollment source")
    private String source;
    private String note;
}
