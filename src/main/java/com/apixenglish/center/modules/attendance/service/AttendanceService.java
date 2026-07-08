package com.apixenglish.center.modules.attendance.service;

import com.apixenglish.center.modules.attendance.dto.request.BulkUpdateAttendanceRequest;
import com.apixenglish.center.modules.attendance.dto.request.GenerateSessionsRequest;
import com.apixenglish.center.modules.attendance.dto.response.AttendanceMonitorResponse;
import com.apixenglish.center.modules.attendance.dto.response.AttendanceSheetResponse;
import com.apixenglish.center.modules.attendance.dto.response.ClassSessionResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {
    int generateSessions(UUID classId, GenerateSessionsRequest request);
    List<ClassSessionResponse> listClassSessions(UUID classId, LocalDate fromDate, LocalDate toDate, String status, String attendanceStatus, UUID teacherId);
    List<ClassSessionResponse> getTodayTeacherSessions(UUID userId, LocalDate date, LocalDate fromDate, LocalDate toDate, String status, String attendanceStatus);
    AttendanceSheetResponse getAttendanceSheet(UUID sessionId);
    void bulkUpdateAttendance(UUID sessionId, BulkUpdateAttendanceRequest request, UUID userId);
    void bulkMarkPresent(UUID sessionId, UUID userId);
    void completeAttendance(UUID sessionId, UUID userId, Boolean confirm);
    AttendanceMonitorResponse monitorAttendance(LocalDate date);
}
