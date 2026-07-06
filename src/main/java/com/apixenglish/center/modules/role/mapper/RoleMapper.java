package com.apixenglish.center.modules.role.mapper;

import com.apixenglish.center.modules.role.dto.response.PermissionSummary;
import com.apixenglish.center.modules.role.dto.response.RoleDetailResponse;
import com.apixenglish.center.modules.role.dto.response.RoleLookupResponse;
import com.apixenglish.center.modules.role.dto.response.RoleResponse;
import com.apixenglish.center.modules.role.entity.Role;
import com.apixenglish.center.modules.role.entity.RolePermission;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class RoleMapper {

    public RoleResponse toResponse(Role role, long permissionCount) {
        if (role == null) {
            return null;
        }

        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(role.getIsSystem())
                .isActive(role.getIsActive())
                .permissionCount(permissionCount)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    public RoleDetailResponse toDetailResponse(Role role, List<RolePermission> rolePermissions) {
        if (role == null) {
            return null;
        }

        List<PermissionSummary> permissionSummaries = rolePermissions == null ? List.of() : rolePermissions.stream()
                .filter(rp -> rp.getDeletedAt() == null && rp.getPermission() != null && rp.getPermission().getDeletedAt() == null)
                .map(rp -> PermissionSummary.builder()
                        .id(rp.getPermission().getId())
                        .code(rp.getPermission().getCode())
                        .module(rp.getPermission().getModule())
                        .action(rp.getPermission().getAction())
                        .description(rp.getPermission().getDescription())
                        .isActive(rp.getPermission().getIsActive())
                        .build())
                .toList();

        return RoleDetailResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(role.getIsSystem())
                .isActive(role.getIsActive())
                .permissions(permissionSummaries)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    public RoleLookupResponse toLookupResponse(Role role) {
        if (role == null) {
            return null;
        }

        return RoleLookupResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .displayName("[" + role.getCode() + "] " + role.getName())
                .isSystem(role.getIsSystem())
                .isActive(role.getIsActive())
                .build();
    }
}
