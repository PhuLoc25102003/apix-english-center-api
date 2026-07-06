package com.apixenglish.center.modules.course.mapper;

import com.apixenglish.center.modules.course.dto.response.CourseResponse;
import com.apixenglish.center.modules.course.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseResponse toResponse(Course course) {
        if (course == null) {
            return null;
        }

        return CourseResponse.builder()
                .id(course.getId())
                .levelId(course.getLevel() != null ? course.getLevel().getId() : null)
                .levelName(course.getLevel() != null ? course.getLevel().getName() : null)
                .code(course.getCode())
                .name(course.getName())
                .description(course.getDescription())
                .totalLessons(course.getTotalLessons())
                .durationMinutes(course.getDurationMinutes())
                .defaultMonthlyTuitionFee(course.getDefaultMonthlyTuitionFee())
                .status(course.getStatus())
                .build();
    }
}
