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
public class ClassStudentResponse {
    private UUID enrollmentId;
    private UUID studentId;
    private String studentCode;
    private String studentFullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String schoolName;
    private String grade;
    private String enrollmentStatus;
    private LocalDate enrolledDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;
}
