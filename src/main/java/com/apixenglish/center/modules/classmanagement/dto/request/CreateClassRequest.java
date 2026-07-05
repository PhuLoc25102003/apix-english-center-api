package com.apixenglish.center.modules.classmanagement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassRequest {

    @NotNull(message = "Course ID must not be null")
    private UUID courseId;

    @NotNull(message = "Campus ID must not be null")
    private UUID campusId;

    @NotBlank(message = "Class name must not be blank")
    private String name;

    @NotNull(message = "Capacity must not be null")
    @Min(value = 1, message = "Capacity must be greater than 0")
    private Integer capacity;

    @NotNull(message = "Start date must not be null")
    private LocalDate startDate;

    @NotNull(message = "Expected end date must not be null")
    private LocalDate expectedEndDate;

    @NotBlank(message = "Status must not be blank")
    private String status; // PLANNING, OPEN, ACTIVE, CLOSED, CANCELLED

    private String note;
}
