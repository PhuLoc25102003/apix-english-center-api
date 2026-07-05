package com.apixenglish.center.modules.classmanagement.service.impl;

import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.common.exception.ConflictException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.modules.classmanagement.dto.request.EnrollStudentRequest;
import com.apixenglish.center.modules.classmanagement.dto.response.EnrollmentResponse;
import com.apixenglish.center.modules.classmanagement.entity.ClassEnrollment;
import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import com.apixenglish.center.modules.classmanagement.mapper.ClassEnrollmentMapper;
import com.apixenglish.center.modules.classmanagement.repository.ClassEnrollmentRepository;
import com.apixenglish.center.modules.classmanagement.repository.ClazzRepository;
import com.apixenglish.center.modules.classmanagement.service.ClassEnrollmentService;
import com.apixenglish.center.modules.student.entity.Student;
import com.apixenglish.center.modules.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassEnrollmentServiceImpl implements ClassEnrollmentService {

    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final ClazzRepository clazzRepository;
    private final StudentRepository studentRepository;
    private final ClassEnrollmentMapper classEnrollmentMapper;

    @Override
    @Transactional
    public EnrollmentResponse enrollStudent(EnrollStudentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Clazz clazz = clazzRepository.findById(request.getClassId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        // Check duplicate active enrollment in class
        if (classEnrollmentRepository.existsByStudentIdAndClazzIdAndStatusAndDeletedAtIsNull(
                request.getStudentId(), request.getClassId(), "ACTIVE")) {
            throw new ConflictException("Student already has an active enrollment in this class");
        }

        // Check class capacity limit
        long activeCount = classEnrollmentRepository.countByClazzIdAndStatusAndDeletedAtIsNull(request.getClassId(), "ACTIVE");
        if (activeCount >= clazz.getCapacity()) {
            throw new BusinessException("Class capacity exceeded", "CLASS_CAPACITY_EXCEEDED");
        }

        String enrollmentCode = generateNextEnrollmentCode();

        ClassEnrollment enrollment = ClassEnrollment.builder()
                .clazz(clazz)
                .student(student)
                .enrollmentCode(enrollmentCode)
                .enrolledDate(LocalDate.now())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status("ACTIVE")
                .source(request.getSource())
                .note(request.getNote())
                .build();

        ClassEnrollment savedEnrollment = classEnrollmentRepository.save(enrollment);
        return classEnrollmentMapper.toResponse(savedEnrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudent(UUID studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found");
        }
        return classEnrollmentRepository.findByStudentIdAndDeletedAtIsNull(studentId)
                .stream()
                .map(classEnrollmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByClass(UUID classId) {
        if (!clazzRepository.existsById(classId)) {
            throw new ResourceNotFoundException("Class not found");
        }
        return classEnrollmentRepository.findByClazzIdAndDeletedAtIsNull(classId)
                .stream()
                .map(classEnrollmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EnrollmentResponse cancelEnrollment(UUID id) {
        ClassEnrollment enrollment = classEnrollmentRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        enrollment.setStatus("CANCELLED");
        ClassEnrollment updated = classEnrollmentRepository.save(enrollment);
        return classEnrollmentMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public EnrollmentResponse completeEnrollment(UUID id) {
        ClassEnrollment enrollment = classEnrollmentRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        enrollment.setStatus("COMPLETED");
        ClassEnrollment updated = classEnrollmentRepository.save(enrollment);
        return classEnrollmentMapper.toResponse(updated);
    }

    private synchronized String generateNextEnrollmentCode() {
        String maxCode = classEnrollmentRepository.findMaxEnrollmentCode();
        if (maxCode == null) {
            return "ENR000001";
        }
        try {
            int numericPart = Integer.parseInt(maxCode.substring(3));
            return String.format("ENR%06d", numericPart + 1);
        } catch (Exception e) {
            return "ENR" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
    }
}
