package com.apixenglish.center.modules.classmanagement.service.impl;

import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.modules.classmanagement.dto.request.AssignClassStaffRequest;
import com.apixenglish.center.modules.classmanagement.dto.response.ClassResponse;
import com.apixenglish.center.modules.classmanagement.dto.response.ClassStaffResponse;
import com.apixenglish.center.modules.classmanagement.entity.ClassStaff;
import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import com.apixenglish.center.modules.classmanagement.mapper.ClassStaffMapper;
import com.apixenglish.center.modules.classmanagement.mapper.ClazzMapper;
import com.apixenglish.center.modules.classmanagement.repository.ClassStaffRepository;
import com.apixenglish.center.modules.classmanagement.repository.ClazzRepository;
import com.apixenglish.center.modules.classmanagement.service.ClassStaffService;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassStaffServiceImpl implements ClassStaffService {

    private final ClassStaffRepository classStaffRepository;
    private final ClazzRepository clazzRepository;
    private final EmployeeRepository employeeRepository;
    private final ClassStaffMapper classStaffMapper;
    private final ClazzMapper clazzMapper;

    @Override
    @Transactional
    public ClassStaffResponse assignStaff(UUID classId, AssignClassStaffRequest request) {
        Clazz clazz = clazzRepository.findById(classId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Check if there is already an active assignment of the same person in the class with the same role
        List<ClassStaff> existing = classStaffRepository.findByClazzIdAndDeletedAtIsNull(classId);
        boolean duplicate = existing.stream()
                .anyMatch(cs -> cs.getEmployee().getId().equals(request.getEmployeeId()) 
                        && cs.getStaffRole().equals(request.getStaffType())
                        && (cs.getEndDate() == null || !cs.getEndDate().isBefore(request.getStartDate())));
        if (duplicate) {
            throw new BusinessException("Employee is already assigned to this class with the same role during this period", "DUPLICATE_ASSIGNMENT");
        }

        // If isPrimary is true, unset other primary staff with the same role in the class
        if (request.getIsPrimary()) {
            List<ClassStaff> primaryStaff = classStaffRepository.findByClazzIdAndStaffRoleAndIsPrimaryAndDeletedAtIsNull(classId, request.getStaffType(), true);
            for (ClassStaff ps : primaryStaff) {
                ps.setIsPrimary(false);
                classStaffRepository.save(ps);
            }
        }

        ClassStaff classStaff = ClassStaff.builder()
                .clazz(clazz)
                .employee(employee)
                .staffRole(request.getStaffType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isPrimary(request.getIsPrimary())
                .build();

        return classStaffMapper.toResponse(classStaffRepository.save(classStaff));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassStaffResponse> listClassStaff(UUID classId) {
        if (!clazzRepository.existsById(classId)) {
            throw new ResourceNotFoundException("Class not found");
        }
        return classStaffRepository.findByClazzIdAndDeletedAtIsNull(classId).stream()
                .map(classStaffMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deactivateClassStaff(UUID classId, UUID classStaffId) {
        ClassStaff staff = classStaffRepository.findById(classStaffId)
                .filter(cs -> cs.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class staff assignment not found"));

        if (!staff.getClazz().getId().equals(classId)) {
            throw new BusinessException("Class staff assignment does not belong to this class", "INVALID_CLASS");
        }

        staff.setEndDate(LocalDate.now());
        staff.delete();
        classStaffRepository.save(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassResponse> getMyClasses(UUID userId) {
        Employee employee = employeeRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user: " + userId));

        List<ClassStaff> activeAssignments = classStaffRepository.findActiveAssignments(employee.getId(), LocalDate.now());
        return activeAssignments.stream()
                .map(cs -> clazzMapper.toResponse(cs.getClazz()))
                .distinct()
                .collect(Collectors.toList());
    }
}
