package com.apixenglish.center.modules.classmanagement.dto.response;

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
public class EnrollmentResponse {
    private UUID id;
    private UUID classId;
    private String className;
    private String classCode;
    private UUID studentId;
    private String studentName;
    private String studentCode;
    private String enrollmentCode;
    private LocalDate enrolledDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String source;
    private String note;
}
