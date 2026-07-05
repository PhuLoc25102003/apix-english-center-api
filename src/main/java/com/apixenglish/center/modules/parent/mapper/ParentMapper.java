package com.apixenglish.center.modules.parent.mapper;

import com.apixenglish.center.modules.parent.dto.response.ParentResponse;
import com.apixenglish.center.modules.parent.entity.Parent;
import org.springframework.stereotype.Component;

@Component
public class ParentMapper {

    public ParentResponse toResponse(Parent parent) {
        if (parent == null) {
            return null;
        }

        return ParentResponse.builder()
                .id(parent.getId())
                .userId(parent.getUser() != null ? parent.getUser().getId() : null)
                .parentCode(parent.getParentCode())
                .fullName(parent.getFullName())
                .phone(parent.getPhone())
                .email(parent.getEmail())
                .address(parent.getAddress())
                .jobTitle(parent.getJobTitle())
                .note(parent.getNote())
                .build();
    }
}
