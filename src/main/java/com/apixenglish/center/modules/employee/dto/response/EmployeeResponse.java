package com.apixenglish.center.modules.employee.dto.response;

import com.apixenglish.center.modules.position.dto.response.PositionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private UUID id;
    private UUID userId;
    private String employeeCode;
    private String fullName;
    private String email;
    private String phone;
    private List<PositionResponse> positions;
    private UUID campusId;
    private String campusName;
    private LocalDate hiredDate;
    private LocalDate resignedDate;
    private String workingStatus; // Maps to employmentStatus
    private String dateOfBirth;
    private String gender;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;
}
