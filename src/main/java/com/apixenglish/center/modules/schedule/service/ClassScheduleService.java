package com.apixenglish.center.modules.schedule.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.schedule.dto.request.CreateClassScheduleRequest;
import com.apixenglish.center.modules.schedule.dto.request.CreateSchedulePatternRequest;
import com.apixenglish.center.modules.schedule.dto.response.ClassScheduleResponse;
import com.apixenglish.center.modules.schedule.dto.response.SchedulePatternResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface ClassScheduleService {
    PageResponse<ClassScheduleResponse> listSchedules(String search, UUID classId, UUID teacherId, UUID roomId, UUID campusId, String schedulePattern, String status, Pageable pageable);
    ClassScheduleResponse getScheduleById(UUID id);
    SchedulePatternResponse createSchedulePattern(CreateSchedulePatternRequest request);
    ClassScheduleResponse createSchedule(CreateClassScheduleRequest request);
    ClassScheduleResponse updateSchedule(UUID id, CreateClassScheduleRequest request);
    void deactivateSchedule(UUID id);
    int generateSessionsForSchedule(UUID classId, LocalDate fromDate, LocalDate toDate, boolean regenerate);
}
