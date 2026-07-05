package com.apixenglish.center.modules.course.mapper;

import com.apixenglish.center.modules.course.dto.response.LevelResponse;
import com.apixenglish.center.modules.course.entity.Level;
import org.springframework.stereotype.Component;

@Component
public class LevelMapper {

    public LevelResponse toResponse(Level level) {
        if (level == null) {
            return null;
        }

        return LevelResponse.builder()
                .id(level.getId())
                .code(level.getCode())
                .name(level.getName())
                .orderIndex(level.getOrderIndex())
                .description(level.getDescription())
                .isActive(level.getIsActive())
                .build();
    }
}
