package com.apixenglish.center.modules.classmanagement.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class BulkEnrollStudentsRequest {
    @NotEmpty(message = "Student IDs list must not be empty")
    private List<UUID> studentIds;

    @NotNull(message = "Enrolled date must not be null")
    private LocalDate enrolledDate;

    @NotNull(message = "Start date must not be null")
    private LocalDate startDate;

    private String status; // TRIAL, ACTIVE, FROZEN

    private String source; // WALK_IN, REFERRAL, ONLINE, OTHER

    private String note;
}
