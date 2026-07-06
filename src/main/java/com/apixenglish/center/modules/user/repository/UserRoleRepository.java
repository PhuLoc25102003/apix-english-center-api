package com.apixenglish.center.modules.user.repository;

import com.apixenglish.center.modules.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUserIdAndDeletedAtIsNull(UUID userId);

    List<UserRole> findByUserIdAndRoleIdAndDeletedAtIsNull(UUID userId, UUID roleId);

    boolean existsByRoleIdAndDeletedAtIsNull(UUID roleId);
}
