package com.apixenglish.center.modules.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassSessionResponse {
    private UUID id;
    private UUID classId;
    private String classCode;
    private String className;
    private UUID roomId;
    private String roomCode;
    private String roomName;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer lessonNo;
    private String status;
    private String attendanceStatus;
    private String note;
}
