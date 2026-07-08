package com.apixenglish.center.modules.classmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CancelClassStudentRequest {
    @NotBlank(message = "Reason is required")
    private String reason;

    private LocalDate endDate;
}
