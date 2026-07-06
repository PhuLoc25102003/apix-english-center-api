package com.apixenglish.center.modules.user.controller;

import com.apixenglish.center.common.response.ApiResponse;
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
import com.apixenglish.center.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UUID roleId,
            @RequestParam(required = false) UUID campusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(sortParams[0]);
        if (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])) {
            sortOrder = sortOrder.descending();
        }
        PageResponse<UserResponse> pageResponse = userService.listUsers(
                search, status, roleId, campusId, PageRequest.of(page, size, sortOrder));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Users retrieved successfully"));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<List<UserLookupResponse>>> lookupUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status
    ) {
        List<UserLookupResponse> response = userService.lookupUsers(search, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Users retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User details retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('user:lock')")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable UUID id) {
        userService.lockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User locked successfully"));
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('user:unlock')")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable UUID id) {
        userService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User unlocked successfully"));
    }

    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user:reset-password')")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @PathVariable UUID id,
            @RequestBody(required = false) ResetPasswordRequest request
    ) {
        ResetPasswordRequest req = request != null ? request : new ResetPasswordRequest();
        String result = userService.resetPassword(id, req);
        return ResponseEntity.ok(ApiResponse.success(result, "Password reset successful"));
    }

    // Role assignments mappings
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<UserRoleAssignmentResponse>> getUserRoles(@PathVariable UUID id) {
        UserRoleAssignmentResponse response = userService.getUserRoles(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User roles retrieved successfully"));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('user:assign-role')")
    public ResponseEntity<ApiResponse<Void>> assignRoles(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRolesRequest request
    ) {
        userService.assignRoles(id, request);
        return ResponseEntity.ok(ApiResponse.success("Roles assigned successfully"));
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasAuthority('user:remove-role')")
    public ResponseEntity<ApiResponse<Void>> removeRole(
            @PathVariable UUID id,
            @PathVariable UUID roleId,
            @RequestParam(required = false) UUID campusId
    ) {
        userService.removeRole(id, roleId, campusId);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully"));
    }

    @PutMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasAuthority('user:assign-role')")
    public ResponseEntity<ApiResponse<Void>> updateRoleAssignment(
            @PathVariable UUID id,
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleAssignmentRequest request
    ) {
        userService.updateRoleAssignment(id, roleId, request);
        return ResponseEntity.ok(ApiResponse.success("Role assignment updated successfully"));
    }
}
