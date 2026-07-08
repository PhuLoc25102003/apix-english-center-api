package com.apixenglish.center.modules.employee.service.impl;

import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.campus.entity.Campus;
import com.apixenglish.center.modules.campus.repository.CampusRepository;
import com.apixenglish.center.modules.employee.dto.request.CreateEmployeeRequest;
import com.apixenglish.center.modules.employee.dto.request.UpdateEmployeeRequest;
import com.apixenglish.center.modules.employee.dto.response.EmployeeLookupResponse;
import com.apixenglish.center.modules.employee.dto.response.EmployeeResponse;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.employee.entity.EmployeePosition;
import com.apixenglish.center.modules.employee.mapper.EmployeeMapper;
import com.apixenglish.center.modules.employee.repository.EmployeePositionRepository;
import com.apixenglish.center.modules.employee.repository.EmployeeRepository;
import com.apixenglish.center.modules.employee.service.EmployeeService;
import com.apixenglish.center.modules.position.entity.Position;
import com.apixenglish.center.modules.position.repository.PositionRepository;
import com.apixenglish.center.modules.user.entity.User;
import com.apixenglish.center.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeePositionRepository employeePositionRepository;
    private final UserRepository userRepository;
    private final CampusRepository campusRepository;
    private final PositionRepository positionRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> listEmployees(String search, String workingStatus, UUID positionId, UUID campusId, Pageable pageable) {
        Page<Employee> page = employeeRepository.searchEmployees(search, workingStatus, positionId, campusId, pageable);
        return PageResponse.of(page.map(employeeMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeLookupResponse> lookupEmployees() {
        return employeeRepository.findAllActive().stream()
                .map(employeeMapper::toLookupResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .filter(u -> u.getDeletedAt() == null)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        Campus campus = null;
        if (request.getCampusId() != null) {
            campus = campusRepository.findById(request.getCampusId())
                    .filter(c -> c.getDeletedAt() == null)
                    .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));
        }

        String employeeCode = generateNextEmployeeCode();

        Employee employee = Employee.builder()
                .user(user)
                .employeeCode(employeeCode)
                .fullName(request.getFullName())
                .employmentStatus(request.getEmploymentStatus() != null ? request.getEmploymentStatus() : "ACTIVE")
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .hiredDate(request.getHiredDate())
                .resignedDate(request.getResignedDate())
                .campus(campus)
                .note(request.getNote())
                .positions(new ArrayList<>())
                .build();

        Employee saved = employeeRepository.save(employee);

        if (request.getPositionIds() != null && !request.getPositionIds().isEmpty()) {
            boolean isFirst = true;
            for (UUID posId : request.getPositionIds()) {
                Position position = positionRepository.findById(posId)
                        .filter(p -> p.getDeletedAt() == null)
                        .orElseThrow(() -> new ResourceNotFoundException("Position not found: " + posId));

                EmployeePosition ep = EmployeePosition.builder()
                        .employee(saved)
                        .position(position)
                        .isPrimary(isFirst)
                        .effectiveFrom(LocalDate.now())
                        .build();
                employeePositionRepository.save(ep);
                saved.getPositions().add(ep);
                isFirst = false;
            }
        }

        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(UUID id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Campus campus = null;
        if (request.getCampusId() != null) {
            campus = campusRepository.findById(request.getCampusId())
                    .filter(c -> c.getDeletedAt() == null)
                    .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));
        }

        employee.setFullName(request.getFullName());
        employee.setEmploymentStatus(request.getEmploymentStatus());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGender(request.getGender());
        employee.setAddress(request.getAddress());
        employee.setEmergencyContactName(request.getEmergencyContactName());
        employee.setEmergencyContactPhone(request.getEmergencyContactPhone());
        employee.setHiredDate(request.getHiredDate());
        employee.setResignedDate(request.getResignedDate());
        employee.setCampus(campus);
        employee.setNote(request.getNote());

        Employee saved = employeeRepository.save(employee);

        // Update positions mapping
        if (request.getPositionIds() != null) {
            // Remove previous active positions not in the request
            saved.getPositions().removeIf(ep -> {
                if (!request.getPositionIds().contains(ep.getPosition().getId())) {
                    ep.delete();
                    employeePositionRepository.save(ep);
                    return true;
                }
                return false;
            });

            // Add new ones
            List<UUID> existingPosIds = saved.getPositions().stream()
                    .map(ep -> ep.getPosition().getId())
                    .collect(Collectors.toList());

            boolean hasPrimary = saved.getPositions().stream().anyMatch(EmployeePosition::getIsPrimary);

            for (UUID posId : request.getPositionIds()) {
                if (!existingPosIds.contains(posId)) {
                    Position position = positionRepository.findById(posId)
                            .filter(p -> p.getDeletedAt() == null)
                            .orElseThrow(() -> new ResourceNotFoundException("Position not found: " + posId));

                    EmployeePosition ep = EmployeePosition.builder()
                            .employee(saved)
                            .position(position)
                            .isPrimary(!hasPrimary)
                            .effectiveFrom(LocalDate.now())
                            .build();
                    employeePositionRepository.save(ep);
                    saved.getPositions().add(ep);
                    hasPrimary = true;
                }
            }
        }

        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deactivateEmployee(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        employee.setEmploymentStatus("TERMINATED");
        employee.setResignedDate(LocalDate.now());
        employee.delete();
        employeeRepository.save(employee);
    }

    private synchronized String generateNextEmployeeCode() {
        String maxCode = employeeRepository.findMaxEmployeeCode();
        if (maxCode == null) {
            return "EMP001";
        }
        try {
            int numericPart = Integer.parseInt(maxCode.substring(3));
            return String.format("EMP%03d", numericPart + 1);
        } catch (Exception e) {
            return "EMP" + java.util.UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        }
    }
}
