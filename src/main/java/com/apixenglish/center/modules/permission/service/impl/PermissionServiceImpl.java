package com.apixenglish.center.modules.permission.service.impl;

import com.apixenglish.center.common.exception.ConflictException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.audit.service.AuditService;
import com.apixenglish.center.modules.permission.dto.request.CreatePermissionRequest;
import com.apixenglish.center.modules.permission.dto.request.UpdatePermissionRequest;
import com.apixenglish.center.modules.permission.dto.response.PermissionLookupResponse;
import com.apixenglish.center.modules.permission.dto.response.PermissionResponse;
import com.apixenglish.center.modules.permission.entity.Permission;
import com.apixenglish.center.modules.permission.mapper.PermissionMapper;
import com.apixenglish.center.modules.permission.repository.PermissionRepository;
import com.apixenglish.center.modules.permission.service.PermissionService;
import com.apixenglish.center.modules.role.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionMapper permissionMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionResponse> listPermissions(String search, String module, String action, Boolean isActive, Pageable pageable) {
        Page<Permission> page = permissionRepository.searchPermissions(search, module, action, isActive, pageable);
        return PageResponse.of(page.map(permissionMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
        return permissionMapper.toResponse(permission);
    }

    @Override
    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        if (permissionRepository.existsByCodeAndDeletedAtIsNull(request.getCode())) {
            throw new ConflictException("Permission code already exists", "DUPLICATE_PERMISSION_CODE");
        }

        Permission permission = Permission.builder()
                .code(request.getCode())
                .module(request.getModule())
                .action(request.getAction())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Permission saved = permissionRepository.save(permission);
        recordAudit("PERMISSION_CREATED", saved, null);
        return permissionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(UUID id, UpdatePermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

        if (permissionRepository.existsByCodeAndIdNotAndDeletedAtIsNull(request.getCode(), id)) {
            throw new ConflictException("Permission code already exists", "DUPLICATE_PERMISSION_CODE");
        }

        Map<String, Object> before = snapshot(permission);

        permission.setCode(request.getCode());
        permission.setModule(request.getModule());
        permission.setAction(request.getAction());
        permission.setDescription(request.getDescription());
        permission.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        Permission updated = permissionRepository.save(permission);
        recordAudit("PERMISSION_UPDATED", updated, before);
        return permissionMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deletePermission(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

        // Check if assigned to roles
        if (rolePermissionRepository.existsByPermissionIdAndDeletedAtIsNull(id)) {
            throw new ConflictException("Cannot delete permission because it is assigned to roles", "PERMISSION_ASSIGNED");
        }

        Map<String, Object> before = snapshot(permission);
        permission.delete();
        permissionRepository.save(permission);
        recordAudit("PERMISSION_DELETED", permission, before);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionLookupResponse> lookupPermissions(String search, String module, String action, Boolean isActive) {
        // Exclude deleted and inactive by default unless explicitly specified otherwise
        Boolean activeFilter = isActive != null ? isActive : true;
        List<Permission> list = permissionRepository.lookupPermissions(search, module, action, activeFilter);
        return list.stream().map(permissionMapper::toLookupResponse).toList();
    }

    private Map<String, Object> snapshot(Permission p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", p.getCode());
        map.put("module", p.getModule());
        map.put("action", p.getAction());
        map.put("isActive", p.getIsActive());
        return map;
    }

    private void recordAudit(String action, Permission p, Map<String, Object> before) {
        auditService.record(action, "permission", "Permission", p.getId(), before, snapshot(p));
    }
}
