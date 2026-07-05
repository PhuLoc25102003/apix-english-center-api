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

    private LocalDate startDate;
    private LocalDate endDate;
    private String source;
    private String note;
}
