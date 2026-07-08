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
public class StudentClassHistoryResponse {
    private UUID classId;
    private String classCode;
    private String className;
    private UUID enrollmentId;
    private String enrollmentStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private String teacherName;
}
