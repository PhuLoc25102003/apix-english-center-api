package com.apixenglish.center.modules.position.mapper;

import com.apixenglish.center.modules.position.dto.response.PositionLookupResponse;
import com.apixenglish.center.modules.position.dto.response.PositionResponse;
import com.apixenglish.center.modules.position.entity.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionMapper {

    public PositionResponse toResponse(Position position) {
        if (position == null) return null;
        return PositionResponse.builder()
                .id(position.getId())
                .code(position.getCode())
                .name(position.getName())
                .description(position.getDescription())
                .isTeachingPosition(position.getIsTeachingPosition())
                .isActive(position.getIsActive())
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }

    public PositionLookupResponse toLookupResponse(Position position) {
        if (position == null) return null;
        return PositionLookupResponse.builder()
                .id(position.getId())
                .code(position.getCode())
                .name(position.getName())
                .displayName(position.getName() + " (" + position.getCode() + ")")
                .build();
    }
}
