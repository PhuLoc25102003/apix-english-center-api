package com.apixenglish.center.modules.role.service.impl;

import com.apixenglish.center.common.exception.ConflictException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.audit.service.AuditService;
import com.apixenglish.center.modules.permission.entity.Permission;
import com.apixenglish.center.modules.permission.repository.PermissionRepository;
import com.apixenglish.center.modules.role.dto.request.CreateRoleRequest;
import com.apixenglish.center.modules.role.dto.request.UpdateRoleRequest;
import com.apixenglish.center.modules.role.dto.request.AssignPermissionsRequest;
import com.apixenglish.center.modules.role.dto.response.PermissionSummary;
import com.apixenglish.center.modules.role.dto.response.RoleDetailResponse;
import com.apixenglish.center.modules.role.dto.response.RoleLookupResponse;
import com.apixenglish.center.modules.role.dto.response.RoleResponse;
import com.apixenglish.center.modules.role.entity.Role;
import com.apixenglish.center.modules.role.entity.RolePermission;
import com.apixenglish.center.modules.role.mapper.RoleMapper;
import com.apixenglish.center.modules.role.repository.RolePermissionRepository;
import com.apixenglish.center.modules.role.repository.RoleRepository;
import com.apixenglish.center.modules.role.service.RoleService;
import com.apixenglish.center.modules.user.repository.UserRoleRepository;
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
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleMapper roleMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> listRoles(String search, Boolean isActive, Boolean isSystem, Pageable pageable) {
        Page<Role> page = roleRepository.searchRoles(search, isActive, isSystem, pageable);
        return PageResponse.of(page.map(role -> {
            long count = rolePermissionRepository.countByRoleIdAndDeletedAtIsNull(role.getId());
            return roleMapper.toResponse(role, count);
        }));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetailResponse getRoleById(UUID id) {
        Role role = roleRepository.findById(id)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIdAndDeletedAtIsNull(id);
        return roleMapper.toDetailResponse(role, rolePermissions);
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByCodeAndDeletedAtIsNull(request.getCode())) {
            throw new ConflictException("Role code already exists", "DUPLICATE_ROLE_CODE");
        }

        Role role = Role.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .isSystem(false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Role saved = roleRepository.save(role);
        recordAudit("ROLE_CREATED", saved, null);
        return roleMapper.toResponse(saved, 0L);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(UUID id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (roleRepository.existsByCodeAndIdNotAndDeletedAtIsNull(request.getCode(), id)) {
            throw new ConflictException("Role code already exists", "DUPLICATE_ROLE_CODE");
        }

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            // Standard protection: Do not allow changing the code of system roles
            if (!role.getCode().equals(request.getCode())) {
                throw new ConflictException("Cannot change code of system roles", "SYSTEM_ROLE_CODE_PROTECTED");
            }
        }

        Map<String, Object> before = snapshot(role);

        role.setCode(request.getCode().toUpperCase());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        Role updated = roleRepository.save(role);
        recordAudit("ROLE_UPDATED", updated, before);
        long count = rolePermissionRepository.countByRoleIdAndDeletedAtIsNull(id);
        return roleMapper.toResponse(updated, count);
    }

    @Override
    @Transactional
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new ConflictException("Cannot delete system roles", "SYSTEM_ROLE_PROTECTED");
        }

        // Check if assigned to users
        if (userRoleRepository.existsByRoleIdAndDeletedAtIsNull(id)) {
            throw new ConflictException("Cannot delete role because it is assigned to users", "ROLE_ASSIGNED_TO_USERS");
        }

        Map<String, Object> before = snapshot(role);
        role.delete();
        roleRepository.save(role);
        recordAudit("ROLE_DELETED", role, before);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleLookupResponse> lookupRoles(String search, Boolean isActive) {
        boolean activeFilter = isActive != null ? isActive : true;
        List<Role> roles = roleRepository.lookupRoles(search, activeFilter);
        return roles.stream().map(roleMapper::toLookupResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionSummary> getRolePermissions(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        List<RolePermission> rps = rolePermissionRepository.findByRoleIdAndDeletedAtIsNull(roleId);
        return rps.stream()
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
    }

    @Override
    @Transactional
    public void assignPermissions(UUID roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        for (UUID permId : request.getPermissionIds()) {
            Permission permission = permissionRepository.findById(permId)
                    .filter(p -> p.getDeletedAt() == null && Boolean.TRUE.equals(p.getIsActive()))
                    .orElseThrow(() -> new ResourceNotFoundException("Active permission not found for id: " + permId));

            boolean exists = rolePermissionRepository.findByRoleIdAndPermissionIdAndDeletedAtIsNull(roleId, permId).isPresent();
            if (!exists) {
                RolePermission rp = RolePermission.builder()
                        .role(role)
                        .permission(permission)
                        .build();
                rolePermissionRepository.save(rp);
                auditService.record("ROLE_PERMISSION_ASSIGNED", "role", "RolePermission", rp.getId(), null, Map.of("roleId", roleId, "permissionId", permId));
            }
        }
    }

    @Override
    @Transactional
    public void removePermission(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        // SUPER_ADMIN role validation: Do not allow removing critical permission from Super Admin role if it would break access
        if ("SUPER_ADMIN".equalsIgnoreCase(role.getCode())) {
            throw new ConflictException("Cannot modify permissions of the SUPER_ADMIN system role", "SUPER_ADMIN_PROTECTED");
        }

        RolePermission rp = rolePermissionRepository.findByRoleIdAndPermissionIdAndDeletedAtIsNull(roleId, permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission assignment not found for this role"));

        rp.delete();
        rolePermissionRepository.save(rp);
        auditService.record("ROLE_PERMISSION_REMOVED", "role", "RolePermission", rp.getId(), Map.of("roleId", roleId, "permissionId", permissionId), null);
    }

    private Map<String, Object> snapshot(Role r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", r.getCode());
        map.put("name", r.getName());
        map.put("description", r.getDescription());
        map.put("isActive", r.getIsActive());
        map.put("isSystem", r.getIsSystem());
        return map;
    }

    private void recordAudit(String action, Role r, Map<String, Object> before) {
        auditService.record(action, "role", "Role", r.getId(), before, snapshot(r));
    }
}
