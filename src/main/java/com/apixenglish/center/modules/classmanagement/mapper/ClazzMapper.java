package com.apixenglish.center.modules.classmanagement.mapper;

import com.apixenglish.center.modules.classmanagement.dto.response.ClassResponse;
import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import org.springframework.stereotype.Component;

@Component
public class ClazzMapper {

    public ClassResponse toResponse(Clazz clazz) {
        if (clazz == null) {
            return null;
        }

        return ClassResponse.builder()
                .id(clazz.getId())
                .courseId(clazz.getCourse() != null ? clazz.getCourse().getId() : null)
                .courseName(clazz.getCourse() != null ? clazz.getCourse().getName() : null)
                .campusId(clazz.getCampus() != null ? clazz.getCampus().getId() : null)
                .campusName(clazz.getCampus() != null ? clazz.getCampus().getName() : null)
                .classCode(clazz.getClassCode())
                .name(clazz.getName())
                .capacity(clazz.getCapacity())
                .startDate(clazz.getStartDate())
                .expectedEndDate(clazz.getExpectedEndDate())
                .status(clazz.getStatus())
                .note(clazz.getNote())
                .build();
    }
}
