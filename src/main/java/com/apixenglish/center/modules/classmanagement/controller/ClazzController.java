package com.apixenglish.center.modules.classmanagement.controller;

import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.exception.UnauthorizedException;
import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.attendance.dto.request.GenerateSessionsRequest;
import com.apixenglish.center.modules.attendance.dto.response.ClassSessionResponse;
import com.apixenglish.center.modules.attendance.service.AttendanceService;
import com.apixenglish.center.modules.classmanagement.dto.request.AssignClassStaffRequest;
import com.apixenglish.center.modules.classmanagement.dto.request.CreateClassRequest;
import com.apixenglish.center.modules.classmanagement.dto.request.UpdateClassRequest;
import com.apixenglish.center.modules.classmanagement.dto.request.EnrollStudentToClassRequest;
import com.apixenglish.center.modules.classmanagement.dto.request.BulkEnrollStudentsRequest;
import com.apixenglish.center.modules.classmanagement.dto.request.CancelClassStudentRequest;
import com.apixenglish.center.modules.classmanagement.dto.response.ClassResponse;
import com.apixenglish.center.modules.classmanagement.dto.response.ClassStaffResponse;
import com.apixenglish.center.modules.classmanagement.dto.response.ClassStudentResponse;
import com.apixenglish.center.modules.classmanagement.dto.response.ClassLookupResponse;
import com.apixenglish.center.modules.classmanagement.dto.response.EnrollmentResponse;
import com.apixenglish.center.modules.classmanagement.service.ClassStaffService;
import com.apixenglish.center.modules.classmanagement.service.ClassEnrollmentService;
import com.apixenglish.center.modules.classmanagement.service.ClazzService;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.employee.repository.EmployeeRepository;
import com.apixenglish.center.modules.schedule.service.ClassScheduleService;
import com.apixenglish.center.security.CurrentActor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
public class ClazzController {

    private final ClazzService clazzService;
    private final ClassStaffService classStaffService;
    private final ClassEnrollmentService classEnrollmentService;
    private final AttendanceService attendanceService;
    private final EmployeeRepository employeeRepository;
    private final CurrentActor actor;
    private final ClassScheduleService classScheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getClasses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID finalTeacherId = teacherId;
        if (!actor.has("class:read") && actor.has("class:read-assigned")) {
            Employee employee = employeeRepository.findByUserIdAndDeletedAtIsNull(actor.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));
            finalTeacherId = employee.getId();
        } else if (!actor.has("class:read")) {
            throw new UnauthorizedException("Insufficient permissions to read classes");
        }

        PageResponse<ClassResponse> pageResponse = clazzService.getClasses(search, finalTeacherId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Classes retrieved successfully"));
    }

    @GetMapping("/my-classes")
    @PreAuthorize("hasAnyAuthority('class:read', 'class:read-assigned')")
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getMyClasses() {
        List<ClassResponse> classes = classStaffService.getMyClasses(actor.userId());
        return ResponseEntity.ok(ApiResponse.success(classes, "Assigned classes retrieved successfully"));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('class:read', 'class:read-assigned')")
    public ResponseEntity<ApiResponse<List<ClassLookupResponse>>> lookupClasses() {
        List<ClassLookupResponse> classes = actor.has("class:read")
                ? clazzService.lookupClasses()
                : classStaffService.getMyClasses(actor.userId()).stream()
                    .map(clazz -> ClassLookupResponse.builder().id(clazz.getId()).classCode(clazz.getClassCode())
                            .name(clazz.getName()).displayName(clazz.getClassCode() + " - " + clazz.getName()).build())
                    .toList();
        return ResponseEntity.ok(ApiResponse.success(classes, "Classes lookup retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('class:read', 'class:read-assigned')")
    public ResponseEntity<ApiResponse<ClassResponse>> getClassById(@PathVariable UUID id) {
        if (!actor.has("class:read") && actor.has("class:read-assigned")) {
            Employee employee = employeeRepository.findByUserIdAndDeletedAtIsNull(actor.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));
            boolean assigned = classStaffService.listClassStaff(id).stream()
                    .anyMatch(cs -> cs.getEmployeeId().equals(employee.getId()));
            if (!assigned) {
                throw new UnauthorizedException("You are not assigned to this class");
            }
        }

        ClassResponse response = clazzService.getClassById(id);
        response.setStaff(classStaffService.listClassStaff(id));
        response.setEnrolledStudents(classEnrollmentService.listStudentsInClass(id));
        response.setSchedules(classScheduleService.listSchedules(null, id, null, null, null, null, null,
                PageRequest.of(0, 100)).getContent());
        response.setSessions(attendanceService.listClassSessions(id, null, null, null, null, null));
        return ResponseEntity.ok(ApiResponse.success(response, "Class retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('class:create')")
    public ResponseEntity<ApiResponse<ClassResponse>> createClass(@Valid @RequestBody CreateClassRequest request) {
        ClassResponse response = clazzService.createClass(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Class created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('class:update')")
    public ResponseEntity<ApiResponse<ClassResponse>> updateClass(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClassRequest request
    ) {
        ClassResponse response = clazzService.updateClass(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Class updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('class:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable UUID id) {
        clazzService.deleteClass(id);
        return ResponseEntity.ok(ApiResponse.success("Class deleted successfully"));
    }

    // Class Staff Assignment APIs
    @PostMapping("/{classId}/staff")
    @PreAuthorize("hasAuthority('class-staff:assign')")
    public ResponseEntity<ApiResponse<ClassStaffResponse>> assignStaff(
            @PathVariable UUID classId,
            @Valid @RequestBody AssignClassStaffRequest request
    ) {
        ClassStaffResponse response = classStaffService.assignStaff(classId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Staff assigned to class successfully"));
    }

    @GetMapping("/{classId}/staff")
    @PreAuthorize("hasAuthority('class-staff:read')")
    public ResponseEntity<ApiResponse<List<ClassStaffResponse>>> listStaff(@PathVariable UUID classId) {
        List<ClassStaffResponse> response = classStaffService.listClassStaff(classId);
        return ResponseEntity.ok(ApiResponse.success(response, "Class staff retrieved successfully"));
    }

    @PatchMapping("/{classId}/staff/{classStaffId}/deactivate")
    @PreAuthorize("hasAuthority('class-staff:remove')")
    public ResponseEntity<ApiResponse<Void>> deactivateStaff(
            @PathVariable UUID classId,
            @PathVariable UUID classStaffId
    ) {
        classStaffService.deactivateClassStaff(classId, classStaffId);
        return ResponseEntity.ok(ApiResponse.success("Class staff assignment deactivated successfully"));
    }

    // Class Enrollment / Student assignment APIs
    @PostMapping("/{classId}/students")
    @PreAuthorize("hasAnyAuthority('class-student:assign', 'enrollment:create')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollStudent(
            @PathVariable UUID classId,
            @Valid @RequestBody EnrollStudentToClassRequest request
    ) {
        EnrollmentResponse response = classEnrollmentService.enrollStudentToClass(classId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Student enrolled to class successfully"));
    }

    @PostMapping("/{classId}/students/bulk")
    @PreAuthorize("hasAnyAuthority('class-student:assign', 'enrollment:create')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> bulkEnrollStudents(
            @PathVariable UUID classId,
            @Valid @RequestBody BulkEnrollStudentsRequest request
    ) {
        List<EnrollmentResponse> responses = classEnrollmentService.bulkEnrollStudents(classId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responses, "Students bulk enrolled to class successfully"));
    }

    @GetMapping("/{classId}/students")
    @PreAuthorize("hasAnyAuthority('class-student:read', 'enrollment:read')")
    public ResponseEntity<ApiResponse<List<ClassStudentResponse>>> listStudents(@PathVariable UUID classId) {
        List<ClassStudentResponse> response = classEnrollmentService.listStudentsInClass(classId);
        return ResponseEntity.ok(ApiResponse.success(response, "Class students retrieved successfully"));
    }

    @PatchMapping("/{classId}/students/{studentId}/cancel")
    @PreAuthorize("hasAnyAuthority('class-student:remove', 'enrollment:cancel')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> cancelStudentEnrollment(
            @PathVariable UUID classId,
            @PathVariable UUID studentId,
            @Valid @RequestBody CancelClassStudentRequest request
    ) {
        EnrollmentResponse response = classEnrollmentService.cancelStudentEnrollment(classId, studentId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Student enrollment cancelled successfully"));
    }

    // Class Session / Attendance Management APIs
    @GetMapping("/{classId}/sessions")
    @PreAuthorize("hasAuthority('attendance:read')")
    public ResponseEntity<ApiResponse<List<ClassSessionResponse>>> getClassSessions(
            @PathVariable UUID classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String attendanceStatus,
            @RequestParam(required = false) UUID teacherId
    ) {
        List<ClassSessionResponse> response = attendanceService.listClassSessions(
                classId, fromDate, toDate, status, attendanceStatus, teacherId);
        return ResponseEntity.ok(ApiResponse.success(response, "Class sessions retrieved successfully"));
    }

    @PostMapping("/{classId}/sessions/generate")
    @PreAuthorize("hasAuthority('class-schedule:generate')")
    public ResponseEntity<ApiResponse<Integer>> generateSessions(
            @PathVariable UUID classId,
            @Valid @RequestBody GenerateSessionsRequest request
    ) {
        int count = attendanceService.generateSessions(classId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(count, "Class sessions generated successfully"));
    }
}
