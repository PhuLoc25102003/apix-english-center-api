package com.apixenglish.center.modules.schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassScheduleResponse {
    private UUID id;
    private UUID classId;
    private String classCode;
    private String className;
    private UUID roomId;
    private String roomCode;
    private String roomName;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String status;
    private String patternCode;
    private Instant createdAt;
    private Instant updatedAt;
}
