package com.apixenglish.center.modules.employee.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.employee.dto.request.CreateEmployeeRequest;
import com.apixenglish.center.modules.employee.dto.request.UpdateEmployeeRequest;
import com.apixenglish.center.modules.employee.dto.response.EmployeeLookupResponse;
import com.apixenglish.center.modules.employee.dto.response.EmployeeResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {
    PageResponse<EmployeeResponse> listEmployees(String search, String workingStatus, UUID positionId, UUID campusId, Pageable pageable);
    List<EmployeeLookupResponse> lookupEmployees();
    EmployeeResponse getEmployeeById(UUID id);
    EmployeeResponse createEmployee(CreateEmployeeRequest request);
    EmployeeResponse updateEmployee(UUID id, UpdateEmployeeRequest request);
    void deactivateEmployee(UUID id);
}
