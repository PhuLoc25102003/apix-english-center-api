package com.apixenglish.center.modules.role.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.role.dto.request.CreateRoleRequest;
import com.apixenglish.center.modules.role.dto.request.UpdateRoleRequest;
import com.apixenglish.center.modules.role.dto.request.AssignPermissionsRequest;
import com.apixenglish.center.modules.role.dto.response.PermissionSummary;
import com.apixenglish.center.modules.role.dto.response.RoleDetailResponse;
import com.apixenglish.center.modules.role.dto.response.RoleLookupResponse;
import com.apixenglish.center.modules.role.dto.response.RoleResponse;
import com.apixenglish.center.modules.role.service.RoleService;
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
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(sortParams[0]);
        if (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])) {
            sortOrder = sortOrder.descending();
        }
        PageResponse<RoleResponse> pageResponse = roleService.listRoles(
                search, isActive, isSystem, PageRequest.of(page, size, sortOrder));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Roles retrieved successfully"));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<ApiResponse<List<RoleLookupResponse>>> lookupRoles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive
    ) {
        List<RoleLookupResponse> response = roleService.lookupRoles(search, isActive);
        return ResponseEntity.ok(ApiResponse.success(response, "Roles retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> getRoleById(@PathVariable UUID id) {
        RoleDetailResponse response = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Role details retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Role created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        RoleResponse response = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Role updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('role:deactivate')")
    public ResponseEntity<ApiResponse<RoleResponse>> deactivateRole(@PathVariable UUID id) {
        RoleDetailResponse role = roleService.getRoleById(id);
        UpdateRoleRequest updateReq = UpdateRoleRequest.builder()
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .isActive(false)
                .build();
        RoleResponse response = roleService.updateRole(id, updateReq);
        return ResponseEntity.ok(ApiResponse.success(response, "Role deactivated successfully"));
    }

    // Role-Permission assignment endpoints
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<ApiResponse<List<PermissionSummary>>> getRolePermissions(@PathVariable UUID id) {
        List<PermissionSummary> response = roleService.getRolePermissions(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Role permissions retrieved successfully"));
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:assign-permission')")
    public ResponseEntity<ApiResponse<Void>> assignPermissions(
            @PathVariable UUID id,
            @Valid @RequestBody AssignPermissionsRequest request
    ) {
        roleService.assignPermissions(id, request);
        return ResponseEntity.ok(ApiResponse.success("Permissions assigned successfully"));
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('role:remove-permission')")
    public ResponseEntity<ApiResponse<Void>> removePermission(
            @PathVariable UUID id,
            @PathVariable UUID permissionId
    ) {
        roleService.removePermission(id, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission removed successfully"));
    }
}
