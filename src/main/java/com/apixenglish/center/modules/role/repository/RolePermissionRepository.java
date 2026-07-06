package com.apixenglish.center.modules.role.repository;

import com.apixenglish.center.modules.role.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    boolean existsByPermissionIdAndDeletedAtIsNull(UUID permissionId);

    boolean existsByRoleIdAndDeletedAtIsNull(UUID roleId);

    long countByRoleIdAndDeletedAtIsNull(UUID roleId);

    List<RolePermission> findByRoleIdAndDeletedAtIsNull(UUID roleId);

    Optional<RolePermission> findByRoleIdAndPermissionIdAndDeletedAtIsNull(UUID roleId, UUID permissionId);
}
