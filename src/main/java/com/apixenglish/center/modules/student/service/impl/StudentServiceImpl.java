package com.apixenglish.center.modules.student.service.impl;

import com.apixenglish.center.common.exception.ConflictException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.student.dto.request.CreateStudentRequest;
import com.apixenglish.center.modules.student.dto.request.UpdateStudentRequest;
import com.apixenglish.center.modules.student.dto.response.StudentResponse;
import com.apixenglish.center.modules.student.entity.Student;
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
    public PageResponse<StudentResponse> getStudents(String search, Pageable pageable) {
        Page<Student> studentPage = studentRepository.searchStudents(search, pageable);
        Page<StudentResponse> responsePage = studentPage.map(studentMapper::toResponse);
        return PageResponse.of(responsePage);
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
        validateDuplicateStudent(null, request.getFullName(), request.getDateOfBirth(), request.getUserId());

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        String studentCode = generateNextStudentCode();

        Student student = Student.builder()
                .user(user)
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
                .accessMode(request.getAccessMode())
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

        validateDuplicateStudent(id, request.getFullName(), request.getDateOfBirth(), request.getUserId());

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        student.setUser(user);
        student.setFullName(request.getFullName());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setSchoolName(request.getSchoolName());
        student.setGrade(request.getGrade());
        student.setAvatarUrl(request.getAvatarUrl());
        student.setMedicalNotes(request.getMedicalNotes());
        student.setLearningNotes(request.getLearningNotes());
        student.setStudentType(request.getStudentType());
        student.setAccessMode(request.getAccessMode());
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
}
