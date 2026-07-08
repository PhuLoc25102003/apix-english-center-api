package com.apixenglish.center.modules.attendance.service.impl;

import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.exception.UnauthorizedException;
import com.apixenglish.center.modules.attendance.dto.request.BulkUpdateAttendanceRequest;
import com.apixenglish.center.modules.attendance.dto.request.GenerateSessionsRequest;
import com.apixenglish.center.modules.attendance.dto.response.AttendanceMonitorResponse;
import com.apixenglish.center.modules.attendance.dto.response.AttendanceRecordResponse;
import com.apixenglish.center.modules.attendance.dto.response.AttendanceSheetResponse;
import com.apixenglish.center.modules.attendance.dto.response.ClassSessionResponse;
import com.apixenglish.center.modules.attendance.entity.ClassSession;
import com.apixenglish.center.modules.attendance.entity.ClassSessionAttendanceStatus;
import com.apixenglish.center.modules.attendance.entity.StudentAttendance;
import com.apixenglish.center.modules.attendance.mapper.ClassSessionMapper;
import com.apixenglish.center.modules.attendance.repository.ClassSessionAttendanceStatusRepository;
import com.apixenglish.center.modules.attendance.repository.ClassSessionRepository;
import com.apixenglish.center.modules.attendance.repository.StudentAttendanceRepository;
import com.apixenglish.center.modules.attendance.service.AttendanceService;
import com.apixenglish.center.modules.classmanagement.entity.ClassEnrollment;
import com.apixenglish.center.modules.classmanagement.entity.ClassStaff;
import com.apixenglish.center.modules.classmanagement.repository.ClassEnrollmentRepository;
import com.apixenglish.center.modules.classmanagement.repository.ClassStaffRepository;
import com.apixenglish.center.modules.classmanagement.service.ClassScopeGuard;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.employee.repository.EmployeeRepository;
import com.apixenglish.center.modules.schedule.service.ClassScheduleService;
import com.apixenglish.center.modules.student.entity.Student;
import com.apixenglish.center.modules.student.repository.StudentRepository;
import com.apixenglish.center.modules.user.entity.User;
import com.apixenglish.center.modules.user.repository.UserRepository;
import com.apixenglish.center.security.CurrentActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final ClassSessionRepository classSessionRepository;
    private final StudentAttendanceRepository studentAttendanceRepository;
    private final ClassSessionAttendanceStatusRepository attendanceStatusRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final ClassStaffRepository classStaffRepository;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository students;
    private final UserRepository userRepository;
    private final ClassScheduleService classScheduleService;
    private final ClassSessionMapper classSessionMapper;
    private final CurrentActor actor;
    private final ClassScopeGuard classScopeGuard;

    @Override
    @Transactional
    public int generateSessions(UUID classId, GenerateSessionsRequest request) {
        return classScheduleService.generateSessionsForSchedule(classId, request.getFromDate(), request.getToDate(), request.getRegenerate());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSessionResponse> listClassSessions(UUID classId, LocalDate fromDate, LocalDate toDate, String status, String attendanceStatus, UUID teacherId) {
        enforceAssignedScope(classId);
        List<ClassSession> sessions = classSessionRepository.searchSessions(classId, fromDate, toDate, status, attendanceStatus, teacherId);
        return sessions.stream().map(classSessionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSessionResponse> getTodayTeacherSessions(UUID userId, LocalDate date, LocalDate fromDate, LocalDate toDate, String status, String attendanceStatus) {
        Employee employee = employeeRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

        LocalDate finalFrom = fromDate;
        LocalDate finalTo = toDate;
        if (date != null) {
            finalFrom = date;
            finalTo = date;
        } else if (fromDate == null && toDate == null) {
            finalFrom = LocalDate.now();
            finalTo = LocalDate.now();
        }

        List<ClassSession> sessions = classSessionRepository.searchSessions(null, finalFrom, finalTo, status, attendanceStatus, employee.getId());
        return sessions.stream().map(classSessionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceSheetResponse getAttendanceSheet(UUID sessionId) {
        ClassSession session = classSessionRepository.findById(sessionId)
                .filter(cs -> cs.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class session not found"));
        enforceAssignedScope(session.getClazz().getId());

        // Load or create status
        ClassSessionAttendanceStatus status = attendanceStatusRepository.findBySessionIdAndDeletedAtIsNull(sessionId)
                .orElseGet(() -> {
                    ClassSessionAttendanceStatus newStatus = ClassSessionAttendanceStatus.builder()
                            .session(session)
                            .status("NOT_STARTED")
                            .dueAt(Instant.now().plusSeconds(1800))
                            .build();
                    return attendanceStatusRepository.save(newStatus);
                });

        // Sync missing placeholders
        List<ClassEnrollment> enrollments = classEnrollmentRepository.findByClazzIdAndDeletedAtIsNull(session.getClazz().getId());
        List<ClassEnrollment> active = enrollments.stream()
                .filter(e -> List.of("TRIAL", "ACTIVE", "FROZEN").contains(e.getStatus()))
                .collect(Collectors.toList());

        List<StudentAttendance> currentAtts = studentAttendanceRepository.findBySessionIdAndDeletedAtIsNull(sessionId);
        Set<UUID> currentStudentIds = currentAtts.stream().map(sa -> sa.getStudent().getId()).collect(Collectors.toSet());

        List<StudentAttendance> syncList = new ArrayList<>(currentAtts);

        for (ClassEnrollment enrollment : active) {
            if (!currentStudentIds.contains(enrollment.getStudent().getId())) {
                StudentAttendance sa = StudentAttendance.builder()
                        .session(session)
                        .student(enrollment.getStudent())
                        .status("NOT_MARKED")
                        .source("SYSTEM")
                        .lockedByOffice(false)
                        .build();
                StudentAttendance savedSa = studentAttendanceRepository.save(sa);
                syncList.add(savedSa);
            }
        }

        String primaryTeacherName = "N/A";
        List<ClassStaff> staff = classStaffRepository.findByClazzIdAndStaffRoleAndIsPrimaryAndDeletedAtIsNull(session.getClazz().getId(), "TEACHER", true);
        if (!staff.isEmpty()) {
            primaryTeacherName = staff.get(0).getEmployee().getFullName();
        }

        List<AttendanceRecordResponse> records = syncList.stream().map(sa -> {
            String markedByName = "N/A";
            if (sa.getMarkedBy() != null) {
                User u = userRepository.findById(sa.getMarkedBy()).orElse(null);
                if (u != null) {
                    markedByName = u.getFullName();
                }
            }

            return AttendanceRecordResponse.builder()
                    .attendanceId(sa.getId())
                    .studentId(sa.getStudent().getId())
                    .studentCode(sa.getStudent().getStudentCode())
                    .studentFullName(sa.getStudent().getFullName())
                    .status(sa.getStatus())
                    .source(sa.getSource())
                    .lockedByOffice(sa.getLockedByOffice())
                    .note(sa.getNote())
                    .markedByName(markedByName)
                    .markedAt(sa.getMarkedAt())
                    .build();
        }).collect(Collectors.toList());

        return AttendanceSheetResponse.builder()
                .sessionId(session.getId())
                .sessionDate(session.getSessionDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .classId(session.getClazz().getId())
                .classCode(session.getClazz().getClassCode())
                .className(session.getClazz().getName())
                .teacherName(primaryTeacherName)
                .attendanceStatus(status.getStatus())
                .students(records)
                .build();
    }

    @Override
    @Transactional
    public void bulkUpdateAttendance(UUID sessionId, BulkUpdateAttendanceRequest request, UUID userId) {
        ClassSession session = classSessionRepository.findById(sessionId)
                .filter(cs -> cs.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class session not found"));
        enforceAssignedScope(session.getClazz().getId());

        ClassSessionAttendanceStatus status = attendanceStatusRepository.findBySessionIdAndDeletedAtIsNull(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance sheet status not found"));

        boolean isOffice = actor.has("attendance:office-override") || actor.has("attendance:review");

        for (BulkUpdateAttendanceRequest.UpdateRecord record : request.getRecords()) {
            StudentAttendance sa = studentAttendanceRepository.findBySessionIdAndStudentIdAndDeletedAtIsNull(sessionId, record.getStudentId())
                    .orElseGet(() -> {
                        Student s = students.findById(record.getStudentId())
                                .filter(x -> x.getDeletedAt() == null)
                                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + record.getStudentId()));
                        return StudentAttendance.builder()
                                .session(session)
                                .student(s)
                                .status("NOT_MARKED")
                                .source("SYSTEM")
                                .lockedByOffice(false)
                                .build();
                    });

            if (sa.getLockedByOffice() && !isOffice) {
                // Teachers cannot overwrite office-locked records
                continue;
            }

            sa.setStatus(record.getStatus());
            sa.setNote(record.getNote());
            sa.setMarkedBy(userId);
            sa.setMarkedAt(Instant.now());
            sa.setSource(isOffice ? "OFFICE_STAFF" : "TEACHER");
            if (isOffice) {
                sa.setLockedByOffice(true);
            }

            studentAttendanceRepository.save(sa);
        }

        if ("NOT_STARTED".equals(status.getStatus())) {
            status.setStatus("IN_PROGRESS");
            attendanceStatusRepository.save(status);
        }
    }

    @Override
    @Transactional
    public void bulkMarkPresent(UUID sessionId, UUID userId) {
        ClassSession session = classSessionRepository.findById(sessionId)
                .filter(cs -> cs.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class session not found"));
        enforceAssignedScope(session.getClazz().getId());

        List<StudentAttendance> atts = studentAttendanceRepository.findBySessionIdAndDeletedAtIsNull(sessionId);

        boolean isOffice = actor.has("attendance:office-override") || actor.has("attendance:review");

        for (StudentAttendance sa : atts) {
            if ("NOT_MARKED".equals(sa.getStatus()) && (!sa.getLockedByOffice() || isOffice)) {
                sa.setStatus("PRESENT");
                sa.setMarkedBy(userId);
                sa.setMarkedAt(Instant.now());
                sa.setSource(isOffice ? "OFFICE_STAFF" : "TEACHER");
                studentAttendanceRepository.save(sa);
            }
        }
    }

    @Override
    @Transactional
    public void completeAttendance(UUID sessionId, UUID userId, Boolean confirm) {
        ClassSession session = classSessionRepository.findById(sessionId)
                .filter(cs -> cs.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class session not found"));
        enforceAssignedScope(session.getClazz().getId());

        ClassSessionAttendanceStatus status = attendanceStatusRepository.findBySessionIdAndDeletedAtIsNull(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance sheet status not found"));

        List<StudentAttendance> atts = studentAttendanceRepository.findBySessionIdAndDeletedAtIsNull(sessionId);

        boolean hasNotMarked = atts.stream().anyMatch(sa -> "NOT_MARKED".equals(sa.getStatus()));
        if (hasNotMarked && (confirm == null || !confirm)) {
            throw new BusinessException("Cannot complete attendance sheet: there are unmarked students.", "UNMARKED_STUDENTS_PRESENT");
        }

        // Auto mark remaining unmarked students as PRESENT if confirmed
        if (hasNotMarked) {
            for (StudentAttendance sa : atts) {
                if ("NOT_MARKED".equals(sa.getStatus())) {
                    sa.setStatus("PRESENT");
                    sa.setMarkedBy(userId);
                    sa.setMarkedAt(Instant.now());
                    sa.setSource("SYSTEM");
                    studentAttendanceRepository.save(sa);
                }
            }
        }

        Employee employee = employeeRepository.findByUserIdAndDeletedAtIsNull(userId).orElse(null);
        UUID empId = employee != null ? employee.getId() : null;

        boolean isOffice = actor.has("attendance:review");
        status.setStatus(isOffice ? "REVIEWED" : "SUBMITTED");
        if (isOffice) {
            status.setReviewedBy(empId);
            status.setReviewedAt(Instant.now());
        } else {
            status.setSubmittedBy(empId);
            status.setSubmittedAt(Instant.now());
        }

        attendanceStatusRepository.save(status);

        session.setStatus("COMPLETED");
        classSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceMonitorResponse monitorAttendance(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        List<ClassSession> todaySessions = classSessionRepository.searchSessions(null, targetDate, targetDate, null, null, null);

        int total = todaySessions.size();
        int pending = 0;
        int overdue = 0;
        int completed = 0;

        List<AttendanceMonitorResponse.AbsentStudentSummary> absentList = new ArrayList<>();

        for (ClassSession cs : todaySessions) {
            ClassSessionAttendanceStatus status = attendanceStatusRepository.findBySessionIdAndDeletedAtIsNull(cs.getId()).orElse(null);
            String attStatus = status != null ? status.getStatus() : "NOT_STARTED";

            if ("SUBMITTED".equals(attStatus) || "REVIEWED".equals(attStatus)) {
                completed++;
            } else if ("OVERDUE".equals(attStatus)) {
                overdue++;
            } else {
                pending++;
            }

            List<StudentAttendance> atts = studentAttendanceRepository.findBySessionIdAndDeletedAtIsNull(cs.getId());
            for (StudentAttendance sa : atts) {
                if (List.of("ABSENT", "EXCUSED").contains(sa.getStatus())) {
                    absentList.add(AttendanceMonitorResponse.AbsentStudentSummary.builder()
                            .studentId(sa.getStudent().getId())
                            .studentCode(sa.getStudent().getStudentCode())
                            .studentFullName(sa.getStudent().getFullName())
                            .classId(cs.getClazz().getId())
                            .className(cs.getClazz().getName())
                            .excuseStatus("EXCUSED".equals(sa.getStatus()) ? "APPROVED" : "PENDING")
                            .note(sa.getNote())
                            .build());
                }
            }
        }

        return AttendanceMonitorResponse.builder()
                .todayClassesCount(total)
                .pendingCount(pending)
                .overdueCount(overdue)
                .completedCount(completed)
                .absentStudents(absentList)
                .build();
    }

    private void enforceAssignedScope(UUID classId) {
        if (actor.has("class:read-assigned") && !actor.has("attendance:office-override")) {
            classScopeGuard.requireAssigned(classId);
        }
    }
}
