package com.apixenglish.center.modules.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceMonitorResponse {
    private int todayClassesCount;
    private int pendingCount;
    private int overdueCount;
    private int completedCount;
    private List<AbsentStudentSummary> absentStudents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AbsentStudentSummary {
        private UUID studentId;
        private String studentCode;
        private String studentFullName;
        private UUID classId;
        private String className;
        private String excuseStatus; // PENDING, APPROVED, REJECTED
        private String note;
    }
}
