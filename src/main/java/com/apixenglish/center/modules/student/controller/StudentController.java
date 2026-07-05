package com.apixenglish.center.modules.student.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.student.dto.request.CreateStudentRequest;
import com.apixenglish.center.modules.student.dto.request.UpdateStudentRequest;
import com.apixenglish.center.modules.student.dto.response.StudentResponse;
import com.apixenglish.center.modules.student.dto.request.LinkStudentParentRequest;
import com.apixenglish.center.modules.student.dto.response.StudentParentResponse;
import com.apixenglish.center.modules.student.service.StudentParentService;
import com.apixenglish.center.modules.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final StudentParentService studentParentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String studentType,
            @RequestParam(required = false) String accessMode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<StudentResponse> pageResponse = studentService.getStudents(search, studentType, accessMode, status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Students retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable UUID id) {
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Student retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Student created successfully"));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStudentRequest request
    ) {
        StudentResponse response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Student updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable UUID id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully"));
    }

    @PostMapping("/{studentId}/parents")
    public ResponseEntity<ApiResponse<StudentParentResponse>> linkStudentParent(
            @PathVariable UUID studentId,
            @Valid @RequestBody LinkStudentParentRequest request
    ) {
        StudentParentResponse response = studentParentService.linkStudentParent(studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Student and Parent linked successfully"));
    }

    @GetMapping("/{studentId}/parents")
    public ResponseEntity<ApiResponse<List<StudentParentResponse>>> getStudentParents(@PathVariable UUID studentId) {
        List<StudentParentResponse> response = studentParentService.getStudentParents(studentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Student parents retrieved successfully"));
    }

    @DeleteMapping("/{studentId}/parents/{parentId}")
    public ResponseEntity<ApiResponse<Void>> unlinkStudentParent(
            @PathVariable UUID studentId,
            @PathVariable UUID parentId
    ) {
        studentParentService.unlinkStudentParent(studentId, parentId);
        return ResponseEntity.ok(ApiResponse.success("Student and Parent unlinked successfully"));
    }
}
