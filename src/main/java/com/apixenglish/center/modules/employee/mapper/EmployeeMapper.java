package com.apixenglish.center.modules.employee.mapper;

import com.apixenglish.center.modules.employee.dto.response.EmployeeLookupResponse;
import com.apixenglish.center.modules.employee.dto.response.EmployeeResponse;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.position.dto.response.PositionResponse;
import com.apixenglish.center.modules.position.mapper.PositionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EmployeeMapper {

    private final PositionMapper positionMapper;

    public EmployeeResponse toResponse(Employee employee) {
        if (employee == null) return null;

        List<PositionResponse> positions = Collections.emptyList();
        if (employee.getPositions() != null) {
            positions = employee.getPositions().stream()
                    .filter(ep -> ep.getDeletedAt() == null)
                    .map(ep -> positionMapper.toResponse(ep.getPosition()))
                    .collect(Collectors.toList());
        }

        return EmployeeResponse.builder()
                .id(employee.getId())
                .userId(employee.getUser() != null ? employee.getUser().getId() : null)
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .email(employee.getUser() != null ? employee.getUser().getEmail() : null)
                .phone(employee.getUser() != null ? employee.getUser().getPhone() : null)
                .positions(positions)
                .campusId(employee.getCampus() != null ? employee.getCampus().getId() : null)
                .campusName(employee.getCampus() != null ? employee.getCampus().getName() : null)
                .hiredDate(employee.getHiredDate())
                .resignedDate(employee.getResignedDate())
                .workingStatus(employee.getEmploymentStatus())
                .dateOfBirth(employee.getDateOfBirth() != null ? employee.getDateOfBirth().toString() : null)
                .gender(employee.getGender())
                .address(employee.getAddress())
                .emergencyContactName(employee.getEmergencyContactName())
                .emergencyContactPhone(employee.getEmergencyContactPhone())
                .note(employee.getNote())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    public EmployeeLookupResponse toLookupResponse(Employee employee) {
        if (employee == null) return null;
        return EmployeeLookupResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .displayName(employee.getFullName() + " (" + employee.getEmployeeCode() + ")")
                .build();
    }
}
