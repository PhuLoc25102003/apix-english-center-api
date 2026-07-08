package com.apixenglish.center.modules.schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateSchedulePatternRequest {
    @NotNull(message = "Class ID is required")
    private UUID classId;

    @NotNull(message = "Room ID is required")
    private UUID roomId;

    @NotBlank(message = "Schedule pattern is required")
    private String schedulePattern;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Min(value = 1, message = "Generate months must be at least 1")
    @Max(value = 12, message = "Generate months cannot exceed 12")
    private Integer generateMonths; // Defaults to 4 in service

    private String status; // ACTIVE, INACTIVE
}
