package com.apixenglish.center.modules.schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulePatternResponse {
    private List<ClassScheduleResponse> schedules;
    private String schedulePattern;
    private int generatedSessionsCount;
    private int generatedAttendanceRecordsCount;
    private LocalDate firstSessionDate;
    private LocalDate lastSessionDate;
}
