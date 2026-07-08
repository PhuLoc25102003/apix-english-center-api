package com.apixenglish.center.modules.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSheetResponse {
    private UUID sessionId;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private UUID classId;
    private String classCode;
    private String className;
    private String teacherName;
    private String attendanceStatus;
    private List<AttendanceRecordResponse> students;
}
