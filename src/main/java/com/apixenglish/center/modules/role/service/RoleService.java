package com.apixenglish.center.modules.role.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.role.dto.request.CreateRoleRequest;
import com.apixenglish.center.modules.role.dto.request.UpdateRoleRequest;
import com.apixenglish.center.modules.role.dto.request.AssignPermissionsRequest;
import com.apixenglish.center.modules.role.dto.response.PermissionSummary;
import com.apixenglish.center.modules.role.dto.response.RoleDetailResponse;
import com.apixenglish.center.modules.role.dto.response.RoleLookupResponse;
import com.apixenglish.center.modules.role.dto.response.RoleResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface RoleService {
    PageResponse<RoleResponse> listRoles(String search, Boolean isActive, Boolean isSystem, Pageable pageable);
    RoleDetailResponse getRoleById(UUID id);
    RoleResponse createRole(CreateRoleRequest request);
    RoleResponse updateRole(UUID id, UpdateRoleRequest request);
    void deleteRole(UUID id);
    List<RoleLookupResponse> lookupRoles(String search, Boolean isActive);

    // Role-Permission assignment
    List<PermissionSummary> getRolePermissions(UUID roleId);
    void assignPermissions(UUID roleId, AssignPermissionsRequest request);
    void removePermission(UUID roleId, UUID permissionId);
}
