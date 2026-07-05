package com.apixenglish.center.modules.student.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.student.dto.request.CreateStudentRequest;
import com.apixenglish.center.modules.student.dto.request.UpdateStudentRequest;
import com.apixenglish.center.modules.student.dto.response.StudentResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudentService {
    PageResponse<StudentResponse> getStudents(String search, Pageable pageable);
    StudentResponse getStudentById(UUID id);
    StudentResponse createStudent(CreateStudentRequest request);
    StudentResponse updateStudent(UUID id, UpdateStudentRequest request);
    void deleteStudent(UUID id);
}
