package com.apixenglish.center.modules.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordResponse {
    private UUID attendanceId;
    private UUID studentId;
    private String studentCode;
    private String studentFullName;
    private String status;
    private String source;
    private Boolean lockedByOffice;
    private String note;
    private String markedByName;
    private Instant markedAt;
}
