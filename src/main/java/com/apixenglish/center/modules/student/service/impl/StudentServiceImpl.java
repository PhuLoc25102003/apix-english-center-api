package com.apixenglish.center.modules.student.service.impl;

import com.apixenglish.center.common.exception.ConflictException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.student.dto.request.CreateStudentRequest;
import com.apixenglish.center.modules.student.dto.request.UpdateStudentRequest;
import com.apixenglish.center.modules.student.dto.response.StudentResponse;
import com.apixenglish.center.modules.student.dto.response.StudentLookupResponse;
import com.apixenglish.center.modules.student.entity.Student;
import com.apixenglish.center.modules.student.entity.StudentType;
import com.apixenglish.center.modules.student.entity.StudentAccessMode;
import com.apixenglish.center.modules.student.entity.StudentStatus;
import com.apixenglish.center.modules.student.mapper.StudentMapper;
import com.apixenglish.center.modules.student.repository.StudentRepository;
import com.apixenglish.center.modules.student.service.StudentService;
import com.apixenglish.center.modules.user.entity.User;
import com.apixenglish.center.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentMapper studentMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> getStudents(
            String search,
            String studentTypeStr,
            String accessModeStr,
            String statusStr,
            Pageable pageable
    ) {
        StudentType studentType = parseEnum(StudentType.class, studentTypeStr);
        StudentAccessMode accessMode = parseEnum(StudentAccessMode.class, accessModeStr);
        StudentStatus status = parseEnum(StudentStatus.class, statusStr);

        Page<Student> studentPage = studentRepository.searchStudents(search, studentType, accessMode, status, pageable);
        Page<StudentResponse> responsePage = studentPage.map(studentMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(UUID id) {
        Student student = studentRepository.findById(id)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return studentMapper.toResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        validateDuplicateStudent(null, request.getFullName(), request.getDateOfBirth(), null);

        String studentCode = generateNextStudentCode();

        Student student = Student.builder()
                .user(null)
                .studentCode(studentCode)
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .schoolName(request.getSchoolName())
                .grade(request.getGrade())
                .avatarUrl(request.getAvatarUrl())
                .medicalNotes(request.getMedicalNotes())
                .learningNotes(request.getLearningNotes())
                .studentType(request.getStudentType())
                .accessMode(StudentAccessMode.NO_ACCOUNT)
                .status(StudentStatus.ACTIVE)
                .build();

        Student savedStudent = studentRepository.save(student);
        return studentMapper.toResponse(savedStudent);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(UUID id, UpdateStudentRequest request) {
        Student student = studentRepository.findById(id)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        validateDuplicateStudent(id, request.getFullName(), request.getDateOfBirth(), null);
        student.setUser(null);

        student.setFullName(request.getFullName());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setSchoolName(request.getSchoolName());
        student.setGrade(request.getGrade());
        student.setAvatarUrl(request.getAvatarUrl());
        student.setMedicalNotes(request.getMedicalNotes());
        student.setLearningNotes(request.getLearningNotes());
        student.setStudentType(request.getStudentType());
        student.setAccessMode(StudentAccessMode.NO_ACCOUNT);
        student.setStatus(request.getStatus());

        Student updatedStudent = studentRepository.save(student);
        return studentMapper.toResponse(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(UUID id) {
        Student student = studentRepository.findById(id)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        student.delete();
        studentRepository.save(student);
    }

    private void validateDuplicateStudent(UUID currentStudentId, String fullName, java.time.LocalDate dateOfBirth, UUID userId) {
        List<Student> existingStudents = studentRepository.findByFullNameAndDateOfBirthAndDeletedAtIsNull(fullName, dateOfBirth);

        String targetPhone = null;
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                targetPhone = user.getPhone();
            }
        }

        for (Student existing : existingStudents) {
            if (currentStudentId != null && existing.getId().equals(currentStudentId)) {
                continue;
            }

            String existingPhone = existing.getUser() != null ? existing.getUser().getPhone() : null;

            if (targetPhone != null && existingPhone != null && targetPhone.equals(existingPhone)) {
                throw new ConflictException("A student with the same full name, date of birth, and phone already exists");
            }

            if (targetPhone == null && existingPhone == null) {
                throw new ConflictException("A student with the same full name and date of birth already exists");
            }
        }
    }

    private synchronized String generateNextStudentCode() {
        String maxCode = studentRepository.findMaxStudentCode();
        if (maxCode == null) {
            return "STU000001";
        }
        try {
            int numericPart = Integer.parseInt(maxCode.substring(3));
            return String.format("STU%06d", numericPart + 1);
        } catch (Exception e) {
            return "STU" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentLookupResponse> lookupStudents() {
        return studentRepository.findByDeletedAtIsNull().stream()
                .map(s -> StudentLookupResponse.builder()
                        .id(s.getId())
                        .studentCode(s.getStudentCode())
                        .fullName(s.getFullName())
                        .displayName(s.getFullName() + " (" + s.getStudentCode() + ")")
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }
}
