package com.apixenglish.center.modules.classmanagement.service;

import com.apixenglish.center.common.exception.ForbiddenException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.modules.classmanagement.repository.ClassStaffRepository;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.employee.repository.EmployeeRepository;
import com.apixenglish.center.security.CurrentActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ClassScopeGuard {
    private final CurrentActor currentActor;
    private final EmployeeRepository employeeRepository;
    private final ClassStaffRepository classStaffRepository;

    public Employee requireAssigned(java.util.UUID classId) {
        Employee employee = employeeRepository.findByUserIdAndDeletedAtIsNull(currentActor.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for current user"));
        if (!classStaffRepository.isAssigned(employee.getId(), classId, LocalDate.now())) {
            throw new ForbiddenException("The current employee is not assigned to this class", "CLASS_SCOPE_DENIED");
        }
        return employee;
    }

    public Employee currentEmployee() {
        return employeeRepository.findByUserIdAndDeletedAtIsNull(currentActor.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for current user"));
    }
}
