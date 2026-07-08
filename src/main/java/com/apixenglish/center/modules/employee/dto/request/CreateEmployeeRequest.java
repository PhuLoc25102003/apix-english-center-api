package com.apixenglish.center.modules.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateEmployeeRequest {
    private UUID userId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String employmentStatus; // ACTIVE, INACTIVE, ON_LEAVE, TERMINATED

    private LocalDate dateOfBirth;

    private String gender;

    private String address;

    private String emergencyContactName;

    private String emergencyContactPhone;

    @NotNull(message = "Hired date is required")
    private LocalDate hiredDate;

    private LocalDate resignedDate;

    private UUID campusId;

    private String note;

    private List<UUID> positionIds;
}
