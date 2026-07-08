package com.apixenglish.center.modules.curriculum.mapper;

import com.apixenglish.center.modules.curriculum.dto.response.CurriculumLookupResponse;
import com.apixenglish.center.modules.curriculum.dto.response.CurriculumResponse;
import com.apixenglish.center.modules.curriculum.entity.Curriculum;
import org.springframework.stereotype.Component;

@Component
public class CurriculumMapper {

    public CurriculumResponse toResponse(Curriculum curriculum) {
        if (curriculum == null) return null;
        return CurriculumResponse.builder()
                .id(curriculum.getId())
                .courseId(curriculum.getCourse() != null ? curriculum.getCourse().getId() : null)
                .courseCode(curriculum.getCourse() != null ? curriculum.getCourse().getCode() : null)
                .courseName(curriculum.getCourse() != null ? curriculum.getCourse().getName() : null)
                .levelId(curriculum.getCourse() != null && curriculum.getCourse().getLevel() != null ? curriculum.getCourse().getLevel().getId() : null)
                .levelName(curriculum.getCourse() != null && curriculum.getCourse().getLevel() != null ? curriculum.getCourse().getLevel().getName() : null)
                .name(curriculum.getName())
                .versionName(curriculum.getVersionName())
                .description(curriculum.getDescription())
                .isActive(curriculum.getIsActive())
                .createdAt(curriculum.getCreatedAt())
                .updatedAt(curriculum.getUpdatedAt())
                .build();
    }

    public CurriculumLookupResponse toLookupResponse(Curriculum curriculum) {
        if (curriculum == null) return null;
        return CurriculumLookupResponse.builder()
                .id(curriculum.getId())
                .name(curriculum.getName())
                .displayName(curriculum.getName() + " (" + curriculum.getVersionName() + ")")
                .build();
    }
}
