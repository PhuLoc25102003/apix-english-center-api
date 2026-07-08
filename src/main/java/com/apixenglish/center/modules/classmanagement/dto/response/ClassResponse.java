package com.apixenglish.center.modules.classmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import com.apixenglish.center.modules.attendance.dto.response.ClassSessionResponse;
import com.apixenglish.center.modules.schedule.dto.response.ClassScheduleResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassResponse {
    private UUID id;
    private UUID courseId;
    private String courseName;
    private UUID campusId;
    private String campusName;
    private String classCode;
    private String name;
    private Integer capacity;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private String status;
    private String note;
    private List<ClassStaffResponse> staff;
    private List<ClassStudentResponse> enrolledStudents;
    private List<ClassScheduleResponse> schedules;
    private List<ClassSessionResponse> sessions;
}
