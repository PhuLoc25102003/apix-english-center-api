package com.apixenglish.center.modules.attendance.mapper;

import com.apixenglish.center.modules.attendance.dto.response.ClassSessionResponse;
import com.apixenglish.center.modules.attendance.entity.ClassSession;
import com.apixenglish.center.modules.attendance.entity.ClassSessionAttendanceStatus;
import com.apixenglish.center.modules.attendance.repository.ClassSessionAttendanceStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClassSessionMapper {

    private final ClassSessionAttendanceStatusRepository attendanceStatusRepository;

    public ClassSessionResponse toResponse(ClassSession session) {
        if (session == null) return null;

        String attStatus = "NOT_STARTED";
        ClassSessionAttendanceStatus status = attendanceStatusRepository.findBySessionIdAndDeletedAtIsNull(session.getId()).orElse(null);
        if (status != null) {
            attStatus = status.getStatus();
        }

        return ClassSessionResponse.builder()
                .id(session.getId())
                .classId(session.getClazz() != null ? session.getClazz().getId() : null)
                .classCode(session.getClazz() != null ? session.getClazz().getClassCode() : null)
                .className(session.getClazz() != null ? session.getClazz().getName() : null)
                .roomId(session.getRoom() != null ? session.getRoom().getId() : null)
                .roomCode(session.getRoom() != null ? session.getRoom().getCode() : null)
                .roomName(session.getRoom() != null ? session.getRoom().getName() : null)
                .sessionDate(session.getSessionDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .lessonNo(session.getLessonNo())
                .status(session.getStatus())
                .attendanceStatus(attStatus)
                .note(session.getNote())
                .build();
    }
}
