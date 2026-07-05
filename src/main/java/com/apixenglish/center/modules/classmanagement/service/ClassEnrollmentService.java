package com.apixenglish.center.modules.classmanagement.service;

import com.apixenglish.center.modules.classmanagement.dto.request.EnrollStudentRequest;
import com.apixenglish.center.modules.classmanagement.dto.response.EnrollmentResponse;

import java.util.List;
import java.util.UUID;

public interface ClassEnrollmentService {
    EnrollmentResponse enrollStudent(EnrollStudentRequest request);
    List<EnrollmentResponse> getEnrollmentsByStudent(UUID studentId);
    List<EnrollmentResponse> getEnrollmentsByClass(UUID classId);
    EnrollmentResponse cancelEnrollment(UUID id);
    EnrollmentResponse completeEnrollment(UUID id);
}
