package com.apixenglish.center.modules.campus.mapper;

import com.apixenglish.center.modules.campus.dto.response.CampusResponse;
import com.apixenglish.center.modules.campus.entity.Campus;
import org.springframework.stereotype.Component;

@Component
public class CampusMapper {

    public CampusResponse toResponse(Campus campus) {
        if (campus == null) {
            return null;
        }

        return CampusResponse.builder()
                .id(campus.getId())
                .code(campus.getCode())
                .name(campus.getName())
                .address(campus.getAddress())
                .phone(campus.getPhone())
                .description(campus.getDescription())
                .isActive(campus.getIsActive())
                .build();
    }
}
