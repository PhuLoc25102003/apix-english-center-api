package com.apixenglish.center.modules.employee.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.employee.dto.request.CreateEmployeeRequest;
import com.apixenglish.center.modules.employee.dto.request.UpdateEmployeeRequest;
import com.apixenglish.center.modules.employee.dto.response.EmployeeLookupResponse;
import com.apixenglish.center.modules.employee.dto.response.EmployeeResponse;
import com.apixenglish.center.modules.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAuthority('employee:read')")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String workingStatus,
            @RequestParam(required = false) UUID positionId,
            @RequestParam(required = false) UUID campusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(sortParams[0]);
        if (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])) {
            sortOrder = sortOrder.descending();
        }
        PageResponse<EmployeeResponse> pageResponse = employeeService.listEmployees(
                search, workingStatus, positionId, campusId, PageRequest.of(page, size, sortOrder));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Employees retrieved successfully"));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('employee:read', 'class:assign_staff', 'class-schedule:create', 'class-schedule:update')")
    public ResponseEntity<ApiResponse<List<EmployeeLookupResponse>>> lookupEmployees() {
        List<EmployeeLookupResponse> list = employeeService.lookupEmployees();
        return ResponseEntity.ok(ApiResponse.success(list, "Employees lookup retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:read')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable UUID id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('employee:create')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Employee created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:update')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('employee:delete', 'employee:resign')")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable UUID id) {
        employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee resigned/deactivated successfully"));
    }
}
