package com.apixenglish.center.modules.attendance.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BulkUpdateAttendanceRequest {
    @NotEmpty(message = "Records list must not be empty")
    private List<UpdateRecord> records;

    @Data
    public static class UpdateRecord {
        @NotNull(message = "Student ID is required")
        private UUID studentId;

        @NotNull(message = "Status is required")
        private String status; // PRESENT, ABSENT, LATE, EXCUSED

        private String note;
    }
}
