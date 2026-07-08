package com.apixenglish.center.modules.student.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.classmanagement.dto.response.StudentClassHistoryResponse;
import com.apixenglish.center.modules.classmanagement.service.ClassEnrollmentService;
import com.apixenglish.center.modules.student.dto.request.CreateStudentRequest;
import com.apixenglish.center.modules.student.dto.request.UpdateStudentRequest;
import com.apixenglish.center.modules.student.dto.response.StudentResponse;
import com.apixenglish.center.modules.student.dto.response.StudentLookupResponse;
import com.apixenglish.center.modules.student.dto.request.LinkStudentParentRequest;
import com.apixenglish.center.modules.student.dto.response.StudentParentResponse;
import com.apixenglish.center.modules.student.service.StudentParentService;
import com.apixenglish.center.modules.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final StudentParentService studentParentService;
    private final ClassEnrollmentService classEnrollmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('student:read')")
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

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('student:read')")
    public ResponseEntity<ApiResponse<List<StudentLookupResponse>>> lookupStudents() {
        List<StudentLookupResponse> list = studentService.lookupStudents();
        return ResponseEntity.ok(ApiResponse.success(list, "Students lookup retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('student:read')")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable UUID id) {
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Student retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('student:create')")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Student created successfully"));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @PreAuthorize("hasAuthority('student:update')")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStudentRequest request
    ) {
        StudentResponse response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Student updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('student:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable UUID id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully"));
    }

    @PostMapping("/{studentId}/parents")
    @PreAuthorize("hasAuthority('student:update')")
    public ResponseEntity<ApiResponse<StudentParentResponse>> linkStudentParent(
            @PathVariable UUID studentId,
            @Valid @RequestBody LinkStudentParentRequest request
    ) {
        StudentParentResponse response = studentParentService.linkStudentParent(studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Student and Parent linked successfully"));
    }

    @GetMapping("/{studentId}/parents")
    @PreAuthorize("hasAuthority('student:read')")
    public ResponseEntity<ApiResponse<List<StudentParentResponse>>> getStudentParents(@PathVariable UUID studentId) {
        List<StudentParentResponse> response = studentParentService.getStudentParents(studentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Student parents retrieved successfully"));
    }

    @DeleteMapping("/{studentId}/parents/{parentId}")
    @PreAuthorize("hasAuthority('student:update')")
    public ResponseEntity<ApiResponse<Void>> unlinkStudentParent(
            @PathVariable UUID studentId,
            @PathVariable UUID parentId
    ) {
        studentParentService.unlinkStudentParent(studentId, parentId);
        return ResponseEntity.ok(ApiResponse.success("Student and Parent unlinked successfully"));
    }

    @GetMapping("/{studentId}/classes")
    @PreAuthorize("hasAuthority('enrollment:read')")
    public ResponseEntity<ApiResponse<List<StudentClassHistoryResponse>>> getStudentClasses(@PathVariable UUID studentId) {
        List<StudentClassHistoryResponse> response = classEnrollmentService.getStudentClassHistory(studentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Student class history retrieved successfully"));
    }
}
