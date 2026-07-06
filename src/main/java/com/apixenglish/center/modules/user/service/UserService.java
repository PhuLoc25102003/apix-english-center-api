package com.apixenglish.center.modules.user.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.user.dto.request.CreateUserRequest;
import com.apixenglish.center.modules.user.dto.request.UpdateUserRequest;
import com.apixenglish.center.modules.user.dto.request.AssignRolesRequest;
import com.apixenglish.center.modules.user.dto.request.UpdateRoleAssignmentRequest;
import com.apixenglish.center.modules.user.dto.request.ResetPasswordRequest;
import com.apixenglish.center.modules.user.dto.response.UserResponse;
import com.apixenglish.center.modules.user.dto.response.UserRoleAssignmentResponse;
import com.apixenglish.center.modules.user.dto.response.UserLookupResponse;
import com.apixenglish.center.modules.user.entity.UserStatus;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface UserService {
    PageResponse<UserResponse> listUsers(String search, UserStatus status, UUID roleId, UUID campusId, Pageable pageable);
    UserResponse getUserById(UUID id);
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(UUID id, UpdateUserRequest request);
    void deleteUser(UUID id);
    void lockUser(UUID id);
    void unlockUser(UUID id);
    String resetPassword(UUID id, ResetPasswordRequest request);
    List<UserLookupResponse> lookupUsers(String search, UserStatus status);

    // User-Role assignments
    UserRoleAssignmentResponse getUserRoles(UUID userId);
    void assignRoles(UUID userId, AssignRolesRequest request);
    void removeRole(UUID userId, UUID roleId, UUID campusId);
    void updateRoleAssignment(UUID userId, UUID roleId, UpdateRoleAssignmentRequest request);
}
