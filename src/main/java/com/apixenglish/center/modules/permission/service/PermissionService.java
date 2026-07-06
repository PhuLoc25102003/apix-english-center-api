package com.apixenglish.center.modules.permission.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.permission.dto.request.CreatePermissionRequest;
import com.apixenglish.center.modules.permission.dto.request.UpdatePermissionRequest;
import com.apixenglish.center.modules.permission.dto.response.PermissionLookupResponse;
import com.apixenglish.center.modules.permission.dto.response.PermissionResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface PermissionService {
    PageResponse<PermissionResponse> listPermissions(String search, String module, String action, Boolean isActive, Pageable pageable);
    PermissionResponse getPermissionById(UUID id);
    PermissionResponse createPermission(CreatePermissionRequest request);
    PermissionResponse updatePermission(UUID id, UpdatePermissionRequest request);
    void deletePermission(UUID id);
    List<PermissionLookupResponse> lookupPermissions(String search, String module, String action, Boolean isActive);
}
