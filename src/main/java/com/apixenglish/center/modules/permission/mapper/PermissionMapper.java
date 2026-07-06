package com.apixenglish.center.modules.permission.mapper;

import com.apixenglish.center.modules.permission.dto.response.PermissionLookupResponse;
import com.apixenglish.center.modules.permission.dto.response.PermissionResponse;
import com.apixenglish.center.modules.permission.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionResponse toResponse(Permission permission) {
        if (permission == null) {
            return null;
        }

        return PermissionResponse.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .module(permission.getModule())
                .action(permission.getAction())
                .description(permission.getDescription())
                .isActive(permission.getIsActive())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }

    public PermissionLookupResponse toLookupResponse(Permission permission) {
        if (permission == null) {
            return null;
        }

        return PermissionLookupResponse.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .module(permission.getModule())
                .action(permission.getAction())
                .displayName(permission.getCode())
                .isActive(permission.getIsActive())
                .build();
    }
}
