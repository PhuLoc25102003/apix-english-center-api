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
public class ClassStaffResponse {
    private UUID id;
    private UUID classId;
    private UUID employeeId;
    private String employeeCode;
    private String employeeName;
    private String staffType; // TEACHER, TEACHING_ASSISTANT
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isPrimary;
}
