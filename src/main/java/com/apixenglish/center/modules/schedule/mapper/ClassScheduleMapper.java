package com.apixenglish.center.modules.schedule.mapper;

import com.apixenglish.center.modules.schedule.dto.response.ClassScheduleResponse;
import com.apixenglish.center.modules.schedule.entity.ClassSchedule;
import org.springframework.stereotype.Component;

@Component
public class ClassScheduleMapper {

    public ClassScheduleResponse toResponse(ClassSchedule schedule) {
        if (schedule == null) return null;
        return ClassScheduleResponse.builder()
                .id(schedule.getId())
                .classId(schedule.getClazz() != null ? schedule.getClazz().getId() : null)
                .classCode(schedule.getClazz() != null ? schedule.getClazz().getClassCode() : null)
                .className(schedule.getClazz() != null ? schedule.getClazz().getName() : null)
                .roomId(schedule.getRoom() != null ? schedule.getRoom().getId() : null)
                .roomCode(schedule.getRoom() != null ? schedule.getRoom().getCode() : null)
                .roomName(schedule.getRoom() != null ? schedule.getRoom().getName() : null)
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .effectiveFrom(schedule.getEffectiveFrom())
                .effectiveTo(schedule.getEffectiveTo())
                .status(schedule.getStatus())
                .patternCode(schedule.getPatternCode())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}
