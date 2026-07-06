package com.apixenglish.center.modules.permission.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.permission.dto.request.CreatePermissionRequest;
import com.apixenglish.center.modules.permission.dto.request.UpdatePermissionRequest;
import com.apixenglish.center.modules.permission.dto.response.PermissionLookupResponse;
import com.apixenglish.center.modules.permission.dto.response.PermissionResponse;
import com.apixenglish.center.modules.permission.service.PermissionService;
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
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(sortParams[0]);
        if (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])) {
            sortOrder = sortOrder.descending();
        }
        PageResponse<PermissionResponse> pageResponse = permissionService.listPermissions(
                search, module, action, isActive, PageRequest.of(page, size, sortOrder));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Permissions retrieved successfully"));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<ApiResponse<List<PermissionLookupResponse>>> lookupPermissions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Boolean isActive
    ) {
        List<PermissionLookupResponse> response = permissionService.lookupPermissions(search, module, action, isActive);
        return ResponseEntity.ok(ApiResponse.success(response, "Permissions retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable UUID id) {
        PermissionResponse response = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Permission retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('permission:create')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        PermissionResponse response = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Permission created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update')")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        PermissionResponse response = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Permission updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable UUID id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success("Permission deleted successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('permission:deactivate')")
    public ResponseEntity<ApiResponse<PermissionResponse>> deactivatePermission(@PathVariable UUID id) {
        PermissionResponse permission = permissionService.getPermissionById(id);
        UpdatePermissionRequest updateReq = UpdatePermissionRequest.builder()
                .code(permission.getCode())
                .module(permission.getModule())
                .action(permission.getAction())
                .description(permission.getDescription())
                .isActive(false)
                .build();
        PermissionResponse response = permissionService.updatePermission(id, updateReq);
        return ResponseEntity.ok(ApiResponse.success(response, "Permission deactivated successfully"));
    }
}
