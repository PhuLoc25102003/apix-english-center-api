package com.apixenglish.center.modules.attendance.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.modules.attendance.dto.request.BulkUpdateAttendanceRequest;
import com.apixenglish.center.modules.attendance.dto.response.AttendanceMonitorResponse;
import com.apixenglish.center.modules.attendance.dto.response.AttendanceSheetResponse;
import com.apixenglish.center.modules.attendance.dto.response.ClassSessionResponse;
import com.apixenglish.center.modules.attendance.service.AttendanceService;
import com.apixenglish.center.security.CurrentActor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final CurrentActor actor;

    @GetMapping("/api/v1/attendance/my-sessions")
    @PreAuthorize("hasAnyAuthority('attendance:read', 'class:read-assigned')")
    public ResponseEntity<ApiResponse<List<ClassSessionResponse>>> getMySessions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String attendanceStatus
    ) {
        List<ClassSessionResponse> list = attendanceService.getTodayTeacherSessions(
                actor.userId(), date, fromDate, toDate, status, attendanceStatus);
        return ResponseEntity.ok(ApiResponse.success(list, "My sessions retrieved successfully"));
    }

    @GetMapping("/api/v1/attendance/monitor")
    @PreAuthorize("hasAuthority('attendance:monitor')")
    public ResponseEntity<ApiResponse<AttendanceMonitorResponse>> monitorAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        AttendanceMonitorResponse response = attendanceService.monitorAttendance(date);
        return ResponseEntity.ok(ApiResponse.success(response, "Attendance monitor board retrieved successfully"));
    }

    @GetMapping("/api/v1/sessions/{sessionId}/attendance")
    @PreAuthorize("hasAuthority('attendance:read')")
    public ResponseEntity<ApiResponse<AttendanceSheetResponse>> getAttendanceSheet(@PathVariable UUID sessionId) {
        AttendanceSheetResponse response = attendanceService.getAttendanceSheet(sessionId);
        return ResponseEntity.ok(ApiResponse.success(response, "Attendance sheet retrieved successfully"));
    }

    @PutMapping("/api/v1/sessions/{sessionId}/attendance")
    @PreAuthorize("hasAuthority('attendance:mark')")
    public ResponseEntity<ApiResponse<Void>> updateAttendance(
            @PathVariable UUID sessionId,
            @Valid @RequestBody BulkUpdateAttendanceRequest request
    ) {
        attendanceService.bulkUpdateAttendance(sessionId, request, actor.userId());
        return ResponseEntity.ok(ApiResponse.success("Attendance sheet updated successfully"));
    }

    @PatchMapping("/api/v1/sessions/{sessionId}/attendance/mark-all-present")
    @PreAuthorize("hasAuthority('attendance:mark-all')")
    public ResponseEntity<ApiResponse<Void>> markAllPresent(@PathVariable UUID sessionId) {
        attendanceService.bulkMarkPresent(sessionId, actor.userId());
        return ResponseEntity.ok(ApiResponse.success("All unmarked students marked present successfully"));
    }

    @PatchMapping("/api/v1/sessions/{sessionId}/attendance/complete")
    @PreAuthorize("hasAuthority('attendance:complete')")
    public ResponseEntity<ApiResponse<Void>> completeAttendance(
            @PathVariable UUID sessionId,
            @RequestParam(defaultValue = "false") Boolean confirm
    ) {
        attendanceService.completeAttendance(sessionId, actor.userId(), confirm);
        return ResponseEntity.ok(ApiResponse.success("Attendance worksheet completed successfully"));
    }
}
