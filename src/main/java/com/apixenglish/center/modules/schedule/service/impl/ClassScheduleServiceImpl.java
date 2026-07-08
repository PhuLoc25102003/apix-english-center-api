package com.apixenglish.center.modules.schedule.service.impl;

import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.attendance.entity.ClassSession;
import com.apixenglish.center.modules.attendance.entity.ClassSessionAttendanceStatus;
import com.apixenglish.center.modules.attendance.entity.StudentAttendance;
import com.apixenglish.center.modules.attendance.repository.ClassSessionAttendanceStatusRepository;
import com.apixenglish.center.modules.attendance.repository.ClassSessionRepository;
import com.apixenglish.center.modules.attendance.repository.StudentAttendanceRepository;
import com.apixenglish.center.modules.classmanagement.entity.ClassEnrollment;
import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import com.apixenglish.center.modules.classmanagement.repository.ClassEnrollmentRepository;
import com.apixenglish.center.modules.classmanagement.repository.ClazzRepository;
import com.apixenglish.center.modules.classmanagement.repository.ClassStaffRepository;
import com.apixenglish.center.modules.room.entity.Room;
import com.apixenglish.center.modules.room.repository.RoomRepository;
import com.apixenglish.center.modules.schedule.dto.request.CreateClassScheduleRequest;
import com.apixenglish.center.modules.schedule.dto.request.CreateSchedulePatternRequest;
import com.apixenglish.center.modules.schedule.dto.response.ClassScheduleResponse;
import com.apixenglish.center.modules.schedule.dto.response.SchedulePatternResponse;
import com.apixenglish.center.modules.schedule.entity.ClassSchedule;
import com.apixenglish.center.modules.schedule.mapper.ClassScheduleMapper;
import com.apixenglish.center.modules.schedule.repository.ClassScheduleRepository;
import com.apixenglish.center.modules.schedule.service.ClassScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassScheduleServiceImpl implements ClassScheduleService {

    private final ClassScheduleRepository classScheduleRepository;
    private final ClazzRepository clazzRepository;
    private final RoomRepository roomRepository;
    private final ClassSessionRepository classSessionRepository;
    private final StudentAttendanceRepository studentAttendanceRepository;
    private final ClassSessionAttendanceStatusRepository attendanceStatusRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final ClassScheduleMapper classScheduleMapper;
    private final ClassStaffRepository classStaffRepository;

    private static class PatternDetails {
        int[] days;
        LocalTime startTime;
        LocalTime endTime;

        PatternDetails(int[] days, LocalTime startTime, LocalTime endTime) {
            this.days = days;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    private PatternDetails getPatternDetails(String pattern) {
        switch (pattern) {
            case "MON_WED_FRI_SLOT_1":
                return new PatternDetails(new int[]{1, 3, 5}, LocalTime.of(18, 0), LocalTime.of(19, 30));
            case "MON_WED_FRI_SLOT_2":
                return new PatternDetails(new int[]{1, 3, 5}, LocalTime.of(19, 30), LocalTime.of(21, 0));
            case "TUE_THU_SAT_SLOT_1":
                return new PatternDetails(new int[]{2, 4, 6}, LocalTime.of(18, 0), LocalTime.of(19, 30));
            case "TUE_THU_SAT_SLOT_2":
                return new PatternDetails(new int[]{2, 4, 6}, LocalTime.of(19, 30), LocalTime.of(21, 0));
            case "SAT_SUN_MORNING":
                return new PatternDetails(new int[]{6, 7}, LocalTime.of(9, 0), LocalTime.of(11, 0));
            case "SAT_SUN_AFTERNOON":
                return new PatternDetails(new int[]{6, 7}, LocalTime.of(15, 0), LocalTime.of(17, 0));
            default:
                throw new BusinessException("Invalid schedule pattern: " + pattern, "INVALID_PATTERN");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClassScheduleResponse> listSchedules(String search, UUID classId, UUID teacherId, UUID roomId, UUID campusId, String schedulePattern, String status, Pageable pageable) {
        Page<ClassSchedule> page = classScheduleRepository.searchSchedules(search, classId, roomId, campusId, schedulePattern, status, teacherId, pageable);
        return PageResponse.of(page.map(classScheduleMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ClassScheduleResponse getScheduleById(UUID id) {
        ClassSchedule schedule = classScheduleRepository.findById(id)
                .filter(cs -> cs.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class schedule not found"));
        return classScheduleMapper.toResponse(schedule);
    }

    @Override
    @Transactional
    public SchedulePatternResponse createSchedulePattern(CreateSchedulePatternRequest request) {
        Clazz clazz = clazzRepository.findById(request.getClassId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        Room room = roomRepository.findById(request.getRoomId())
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        PatternDetails details = getPatternDetails(request.getSchedulePattern());

        List<ClassSchedule> savedSchedules = new ArrayList<>();
        LocalDate effectiveTo = request.getEffectiveTo();
        if (effectiveTo == null) {
            int months = request.getGenerateMonths() != null ? request.getGenerateMonths() : 4;
            effectiveTo = request.getEffectiveFrom().plusMonths(months);
        }

        for (int day : details.days) {
            validateConflicts(null, clazz, room, day, details.startTime, details.endTime,
                    request.getEffectiveFrom(), effectiveTo);
            ClassSchedule schedule = ClassSchedule.builder()
                    .clazz(clazz)
                    .room(room)
                    .dayOfWeek(day)
                    .startTime(details.startTime)
                    .endTime(details.endTime)
                    .effectiveFrom(request.getEffectiveFrom())
                    .effectiveTo(effectiveTo)
                    .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                    .patternCode(request.getSchedulePattern())
                    .build();
            savedSchedules.add(classScheduleRepository.save(schedule));
        }

        // Auto generate sessions and attendance placeholders
        int generateMonths = request.getGenerateMonths() != null ? request.getGenerateMonths() : 4;
        LocalDate endDate = request.getEffectiveFrom().plusMonths(generateMonths);
        int generatedSessionsCount = 0;
        int generatedAttendanceCount = 0;
        LocalDate firstSessionDate = null;
        LocalDate lastSessionDate = null;

        List<ClassEnrollment> enrollments = classEnrollmentRepository.findByClazzIdAndDeletedAtIsNull(clazz.getId());
        List<ClassEnrollment> activeEnrollments = enrollments.stream()
                .filter(e -> List.of("TRIAL", "ACTIVE", "FROZEN").contains(e.getStatus()))
                .collect(Collectors.toList());

        int lessonCounter = 1;

        for (LocalDate date = request.getEffectiveFrom(); !date.isAfter(endDate); date = date.plusDays(1)) {
            int currentDayOfWeek = date.getDayOfWeek().getValue(); // 1=Mon, ..., 7=Sun
            boolean isScheduledDay = false;
            for (int d : details.days) {
                if (d == currentDayOfWeek) {
                    isScheduledDay = true;
                    break;
                }
            }

            if (isScheduledDay) {
                List<ClassSession> existing = classSessionRepository.searchSessions(clazz.getId(), date, date, null, null, null);
                if (!existing.isEmpty()) {
                    continue;
                }
                // Generate ClassSession
                ClassSchedule matchingSchedule = savedSchedules.stream()
                        .filter(schedule -> schedule.getDayOfWeek() == currentDayOfWeek)
                        .findFirst().orElse(null);
                ClassSession session = ClassSession.builder()
                        .clazz(clazz)
                        .room(room)
                        .schedule(matchingSchedule)
                        .sessionDate(date)
                        .startTime(details.startTime)
                        .endTime(details.endTime)
                        .lessonNo(lessonCounter++)
                        .status("PLANNED")
                        .build();

                ClassSession savedSession = classSessionRepository.save(session);
                generatedSessionsCount++;

                if (firstSessionDate == null) {
                    firstSessionDate = date;
                }
                lastSessionDate = date;

                // Create attendance status
                Instant dueAt = LocalDateTime.of(date, details.startTime).plusMinutes(30)
                        .atZone(ZoneId.systemDefault()).toInstant();

                ClassSessionAttendanceStatus attStatus = ClassSessionAttendanceStatus.builder()
                        .session(savedSession)
                        .status("NOT_STARTED")
                        .dueAt(dueAt)
                        .build();
                attendanceStatusRepository.save(attStatus);

                // Create student attendance placeholders
                for (ClassEnrollment enrollment : activeEnrollments) {
                    StudentAttendance sa = StudentAttendance.builder()
                            .session(savedSession)
                            .student(enrollment.getStudent())
                            .status("NOT_MARKED")
                            .source("SYSTEM")
                            .lockedByOffice(false)
                            .build();
                    studentAttendanceRepository.save(sa);
                    generatedAttendanceCount++;
                }
            }
        }

        return SchedulePatternResponse.builder()
                .schedules(savedSchedules.stream().map(classScheduleMapper::toResponse).collect(Collectors.toList()))
                .schedulePattern(request.getSchedulePattern())
                .generatedSessionsCount(generatedSessionsCount)
                .generatedAttendanceRecordsCount(generatedAttendanceCount)
                .firstSessionDate(firstSessionDate)
                .lastSessionDate(lastSessionDate)
                .build();
    }

    @Override
    @Transactional
    public ClassScheduleResponse createSchedule(CreateClassScheduleRequest request) {
        Clazz clazz = clazzRepository.findById(request.getClassId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        Room room = roomRepository.findById(request.getRoomId())
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        validateRequestDates(request);
        validateConflicts(null, clazz, room, request.getDayOfWeek(), request.getStartTime(), request.getEndTime(),
                request.getEffectiveFrom(), request.getEffectiveTo());

        ClassSchedule schedule = ClassSchedule.builder()
                .clazz(clazz)
                .room(room)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .patternCode(request.getPatternCode())
                .build();

        return classScheduleMapper.toResponse(classScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public ClassScheduleResponse updateSchedule(UUID id, CreateClassScheduleRequest request) {
        ClassSchedule schedule = classScheduleRepository.findById(id)
                .filter(value -> value.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class schedule not found"));
        Clazz clazz = clazzRepository.findById(request.getClassId()).filter(value -> value.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        Room room = roomRepository.findById(request.getRoomId()).filter(value -> value.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        validateRequestDates(request);
        validateConflicts(id, clazz, room, request.getDayOfWeek(), request.getStartTime(), request.getEndTime(),
                request.getEffectiveFrom(), request.getEffectiveTo());
        schedule.setClazz(clazz);
        schedule.setRoom(room);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setEffectiveFrom(request.getEffectiveFrom());
        schedule.setEffectiveTo(request.getEffectiveTo());
        schedule.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
        schedule.setPatternCode(request.getPatternCode());
        return classScheduleMapper.toResponse(classScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public void deactivateSchedule(UUID id) {
        ClassSchedule schedule = classScheduleRepository.findById(id)
                .filter(cs -> cs.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class schedule not found"));
        schedule.setStatus("INACTIVE");
        schedule.delete();
        classScheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public int generateSessionsForSchedule(UUID classId, LocalDate fromDate, LocalDate toDate, boolean regenerate) {
        Clazz clazz = clazzRepository.findById(classId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        List<ClassSchedule> schedules = classScheduleRepository.findByClazzIdAndDeletedAtIsNull(classId);
        if (schedules.isEmpty()) {
            throw new BusinessException("No active schedules found for this class", "NO_SCHEDULES");
        }

        if (regenerate) {
            // Delete future planned sessions and their attendance placeholders
            List<ClassSession> futureSessions = classSessionRepository.findFutureSessions(classId, fromDate);
            for (ClassSession session : futureSessions) {
                if ("PLANNED".equals(session.getStatus())) {
                    List<StudentAttendance> atts = studentAttendanceRepository.findBySessionIdAndDeletedAtIsNull(session.getId());
                    for (StudentAttendance att : atts) {
                        att.delete();
                        studentAttendanceRepository.save(att);
                    }
                    ClassSessionAttendanceStatus status = attendanceStatusRepository.findBySessionIdAndDeletedAtIsNull(session.getId()).orElse(null);
                    if (status != null) {
                        status.delete();
                        attendanceStatusRepository.save(status);
                    }
                    session.delete();
                    classSessionRepository.save(session);
                }
            }
        }

        List<ClassEnrollment> enrollments = classEnrollmentRepository.findByClazzIdAndDeletedAtIsNull(clazz.getId());
        List<ClassEnrollment> activeEnrollments = enrollments.stream()
                .filter(e -> List.of("TRIAL", "ACTIVE", "FROZEN").contains(e.getStatus()))
                .collect(Collectors.toList());

        int generatedSessionsCount = 0;
        int lessonCounter = 1;

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            int currentDayOfWeek = date.getDayOfWeek().getValue();
            for (ClassSchedule cs : schedules) {
                if (cs.getDayOfWeek() == currentDayOfWeek && "ACTIVE".equals(cs.getStatus())) {
                    // Check if session already exists for this date and class
                    List<ClassSession> existing = classSessionRepository.searchSessions(classId, date, date, null, null, null);
                    if (existing.isEmpty()) {
                        ClassSession session = ClassSession.builder()
                                .clazz(clazz)
                                .room(cs.getRoom())
                                .schedule(cs)
                                .sessionDate(date)
                                .startTime(cs.getStartTime())
                                .endTime(cs.getEndTime())
                                .lessonNo(lessonCounter++)
                                .status("PLANNED")
                                .build();
                        ClassSession savedSession = classSessionRepository.save(session);
                        generatedSessionsCount++;

                        Instant dueAt = LocalDateTime.of(date, cs.getStartTime()).plusMinutes(30)
                                .atZone(ZoneId.systemDefault()).toInstant();

                        ClassSessionAttendanceStatus attStatus = ClassSessionAttendanceStatus.builder()
                                .session(savedSession)
                                .status("NOT_STARTED")
                                .dueAt(dueAt)
                                .build();
                        attendanceStatusRepository.save(attStatus);

                        for (ClassEnrollment enrollment : activeEnrollments) {
                            StudentAttendance sa = StudentAttendance.builder()
                                    .session(savedSession)
                                    .student(enrollment.getStudent())
                                    .status("NOT_MARKED")
                                    .source("SYSTEM")
                                    .lockedByOffice(false)
                                    .build();
                            studentAttendanceRepository.save(sa);
                        }
                    }
                }
            }
        }

        return generatedSessionsCount;
    }

    private void validateRequestDates(CreateClassScheduleRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException("Schedule start time must be before end time", "INVALID_SCHEDULE_TIME");
        }
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new BusinessException("Schedule effectiveTo must not precede effectiveFrom", "INVALID_SCHEDULE_DATES");
        }
    }

    private void validateConflicts(UUID ignoredScheduleId, Clazz clazz, Room room, int dayOfWeek,
                                   LocalTime startTime, LocalTime endTime, LocalDate effectiveFrom, LocalDate effectiveTo) {
        Set<UUID> classEmployeeIds = classStaffRepository.findByClazzIdAndDeletedAtIsNull(clazz.getId()).stream()
                .filter(staff -> Set.of("TEACHER", "TEACHING_ASSISTANT").contains(staff.getStaffRole()))
                .map(staff -> staff.getEmployee().getId()).collect(Collectors.toSet());
        for (ClassSchedule other : classScheduleRepository.findByDeletedAtIsNullAndStatus("ACTIVE")) {
            if (other.getId().equals(ignoredScheduleId) || other.getDayOfWeek() != dayOfWeek
                    || !dateRangesOverlap(effectiveFrom, effectiveTo, other.getEffectiveFrom(), other.getEffectiveTo())
                    || !startTime.isBefore(other.getEndTime()) || !endTime.isAfter(other.getStartTime())) {
                continue;
            }
            if (other.getClazz().getId().equals(clazz.getId())) {
                throw new BusinessException("The class already has an overlapping schedule", "DUPLICATE_CLASS_SCHEDULE");
            }
            if (other.getRoom().getId().equals(room.getId())) {
                throw new BusinessException("The room is already booked for this time", "ROOM_SCHEDULE_CONFLICT");
            }
            boolean staffConflict = classStaffRepository.findByClazzIdAndDeletedAtIsNull(other.getClazz().getId()).stream()
                    .anyMatch(staff -> classEmployeeIds.contains(staff.getEmployee().getId()));
            if (staffConflict) {
                throw new BusinessException("A teacher or teaching assistant has an overlapping class", "STAFF_SCHEDULE_CONFLICT");
            }
        }
    }

    private boolean dateRangesOverlap(LocalDate fromA, LocalDate toA, LocalDate fromB, LocalDate toB) {
        LocalDate max = LocalDate.of(9999, 12, 31);
        return !fromA.isAfter(toB == null ? max : toB) && !fromB.isAfter(toA == null ? max : toA);
    }
}
