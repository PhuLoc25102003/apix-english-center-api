package com.apixenglish.center.modules.user.mapper;

import com.apixenglish.center.modules.user.dto.response.RoleSummary;
import com.apixenglish.center.modules.user.dto.response.UserLookupResponse;
import com.apixenglish.center.modules.user.dto.response.UserResponse;
import com.apixenglish.center.modules.user.dto.response.UserRoleAssignmentResponse;
import com.apixenglish.center.modules.user.entity.User;
import com.apixenglish.center.modules.user.entity.UserRole;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user, List<UserRole> userRoles) {
        if (user == null) {
            return null;
        }

        List<RoleSummary> rolesList = userRoles == null ? List.of() : userRoles.stream()
                .filter(ur -> ur.getDeletedAt() == null && ur.getRole() != null && ur.getRole().getDeletedAt() == null)
                .map(ur -> RoleSummary.builder()
                        .id(ur.getRole().getId())
                        .code(ur.getRole().getCode())
                        .name(ur.getRole().getName())
                        .build())
                .collect(Collectors.toList());

        List<UserResponse.CampusSummary> campusScopes = userRoles == null ? List.of() : userRoles.stream()
                .filter(ur -> ur.getDeletedAt() == null && ur.getCampus() != null && ur.getCampus().getDeletedAt() == null)
                .map(ur -> UserResponse.CampusSummary.builder()
                        .id(ur.getCampus().getId())
                        .code(ur.getCampus().getCode())
                        .name(ur.getCampus().getName())
                        .build())
                // Distinct by campus ID
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        UserResponse.CampusSummary::getId,
                        c -> c,
                        (c1, c2) -> c1 // keep first in case of duplicates
                ))
                .values()
                .stream()
                .toList();

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .mfaEnabled(user.getMfaEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .roles(rolesList)
                .campusScopes(campusScopes)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserRoleAssignmentResponse toAssignmentResponse(User user, List<UserRole> userRoles) {
        if (user == null) {
            return null;
        }

        List<UserRoleAssignmentResponse.RoleAssignmentItem> items = userRoles == null ? List.of() : userRoles.stream()
                .filter(ur -> ur.getDeletedAt() == null && ur.getRole() != null && ur.getRole().getDeletedAt() == null)
                .map(ur -> UserRoleAssignmentResponse.RoleAssignmentItem.builder()
                        .assignmentId(ur.getId())
                        .roleId(ur.getRole().getId())
                        .roleCode(ur.getRole().getCode())
                        .roleName(ur.getRole().getName())
                        .campusId(ur.getCampus() != null ? ur.getCampus().getId() : null)
                        .campusName(ur.getCampus() != null ? ur.getCampus().getName() : null)
                        .assignedAt(ur.getAssignedAt())
                        .expiredAt(ur.getExpiredAt())
                        .isActive(ur.getIsActive())
                        .build())
                .toList();

        return UserRoleAssignmentResponse.builder()
                .userId(user.getId())
                .roles(items)
                .build();
    }

    public UserLookupResponse toLookupResponse(User user) {
        if (user == null) {
            return null;
        }

        String displayName = user.getFullName();
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            displayName = displayName + " (" + user.getUsername() + ")";
        } else if (user.getEmail() != null && !user.getEmail().isBlank()) {
            displayName = displayName + " (" + user.getEmail() + ")";
        }

        return UserLookupResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .username(user.getUsername())
                .displayName(displayName)
                .build();
    }
}
