package com.apixenglish.center.modules.course.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {

    @NotNull(message = "Level ID must not be null")
    private UUID levelId;

    @NotBlank(message = "Code must not be blank")
    private String code;

    @NotBlank(message = "Name must not be blank")
    private String name;

    private String description;

    @NotNull(message = "Total lessons must not be null")
    @Min(value = 1, message = "Total lessons must be greater than 0")
    private Integer totalLessons;

    @NotNull(message = "Duration minutes must not be null")
    @Min(value = 1, message = "Duration minutes must be greater than 0")
    private Integer durationMinutes;

    @NotNull(message = "Default monthly tuition fee must not be null")
    @DecimalMin(value = "0.0", message = "Default monthly tuition fee must not be negative")
    private BigDecimal defaultMonthlyTuitionFee;

    @NotBlank(message = "Status must not be blank")
    private String status; // DRAFT, ACTIVE, INACTIVE
}
