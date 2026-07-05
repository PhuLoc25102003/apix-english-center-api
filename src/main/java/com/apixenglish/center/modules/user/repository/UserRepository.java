package com.apixenglish.center.modules.user.repository;

import com.apixenglish.center.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    
    boolean existsByEmailAndDeletedAtIsNull(String email);
    
    boolean existsByPhoneAndDeletedAtIsNull(String phone);

    @Query("SELECT DISTINCT p.code FROM UserRole ur " +
           "JOIN ur.role r " +
           "JOIN RolePermission rp ON rp.role = r " +
           "JOIN rp.permission p " +
           "WHERE ur.user.id = :userId " +
           "AND ur.deletedAt IS NULL " +
           "AND ur.isActive = true " +
           "AND (ur.expiredAt IS NULL OR ur.expiredAt > CURRENT_TIMESTAMP) " +
           "AND r.deletedAt IS NULL " +
           "AND r.isActive = true " +
           "AND rp.deletedAt IS NULL " +
           "AND p.deletedAt IS NULL " +
           "AND p.isActive = true")
    List<String> findActivePermissionCodesByUserId(@Param("userId") UUID userId);
}
