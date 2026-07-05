package com.apixenglish.center.modules.classmanagement.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.modules.classmanagement.dto.request.EnrollStudentRequest;
import com.apixenglish.center.modules.classmanagement.dto.response.EnrollmentResponse;
import com.apixenglish.center.modules.classmanagement.service.ClassEnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class ClassEnrollmentController {

    private final ClassEnrollmentService classEnrollmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollStudent(@Valid @RequestBody EnrollStudentRequest request) {
        EnrollmentResponse response = classEnrollmentService.enrollStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Student enrolled successfully"));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getEnrollmentsByStudent(@PathVariable UUID studentId) {
        List<EnrollmentResponse> response = classEnrollmentService.getEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Enrollments retrieved successfully"));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getEnrollmentsByClass(@PathVariable UUID classId) {
        List<EnrollmentResponse> response = classEnrollmentService.getEnrollmentsByClass(classId);
        return ResponseEntity.ok(ApiResponse.success(response, "Enrollments retrieved successfully"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> cancelEnrollment(@PathVariable UUID id) {
        EnrollmentResponse response = classEnrollmentService.cancelEnrollment(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Enrollment cancelled successfully"));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> completeEnrollment(@PathVariable UUID id) {
        EnrollmentResponse response = classEnrollmentService.completeEnrollment(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Enrollment completed successfully"));
    }
}
