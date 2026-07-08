package com.apixenglish.center.modules.employee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLookupResponse {
    private UUID id;
    private String employeeCode;
    private String fullName;
    private String displayName;
}
