package com.apixenglish.center.modules.classmanagement.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.classmanagement.dto.request.*;
import com.apixenglish.center.modules.classmanagement.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ClassEnrollmentService {
    PageResponse<EnrollmentResponse> list(String search, String status, UUID classId, UUID studentId, UUID campusId, String source, LocalDate enrolledFrom, LocalDate enrolledTo, Pageable pageable);
    EnrollmentDetailResponse getDetail(UUID id);
    EnrollmentResponse enrollStudent(EnrollStudentRequest request);
    EnrollmentResponse update(UUID id, UpdateEnrollmentRequest request);
    EnrollmentResponse cancel(UUID id, CancelEnrollmentRequest request);
    EnrollmentResponse transfer(UUID id, TransferEnrollmentRequest request);
    EnrollmentResponse freeze(UUID id, FreezeEnrollmentRequest request);
    EnrollmentResponse complete(UUID id, CompleteEnrollmentRequest request);
    List<EnrollmentResponse> getEnrollmentsByStudent(UUID studentId);
    List<EnrollmentResponse> getEnrollmentsByClass(UUID classId);

    // New Class-specific and Student history APIs
    EnrollmentResponse enrollStudentToClass(UUID classId, EnrollStudentToClassRequest request);
    List<EnrollmentResponse> bulkEnrollStudents(UUID classId, BulkEnrollStudentsRequest request);
    List<ClassStudentResponse> listStudentsInClass(UUID classId);
    EnrollmentResponse cancelStudentEnrollment(UUID classId, UUID studentId, CancelClassStudentRequest request);
    List<StudentClassHistoryResponse> getStudentClassHistory(UUID studentId);
}
