package com.apixenglish.center.modules.user.repository;

import com.apixenglish.center.modules.user.entity.User;
import com.apixenglish.center.modules.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    boolean existsByUsernameAndDeletedAtIsNull(String username);

    boolean existsByUsernameAndIdNotAndDeletedAtIsNull(String username, UUID id);

    boolean existsByEmailAndIdNotAndDeletedAtIsNull(String email, UUID id);

    boolean existsByPhoneAndIdNotAndDeletedAtIsNull(String phone, UUID id);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN UserRole ur ON ur.user = u AND ur.deletedAt IS NULL AND ur.isActive = true " +
           "WHERE u.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR " +
           "     lower(u.fullName) like lower(concat('%', cast(:search as string), '%')) OR " +
           "     lower(u.email) like lower(concat('%', cast(:search as string), '%')) OR " +
           "     lower(u.phone) like lower(concat('%', cast(:search as string), '%')) OR " +
           "     lower(u.username) like lower(concat('%', cast(:search as string), '%')))" +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (cast(:roleId as uuid) IS NULL OR ur.role.id = :roleId) " +
           "AND (cast(:campusId as uuid) IS NULL OR ur.campus.id = :campusId)")
    Page<User> searchUsers(
        @Param("search") String search,
        @Param("status") UserStatus status,
        @Param("roleId") UUID roleId,
        @Param("campusId") UUID campusId,
        Pageable pageable
    );

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (cast(:search as string) IS NULL OR " +
           "     lower(u.fullName) like lower(concat('%', cast(:search as string), '%')) OR " +
           "     lower(u.email) like lower(concat('%', cast(:search as string), '%')) OR " +
           "     lower(u.phone) like lower(concat('%', cast(:search as string), '%')) OR " +
           "     lower(u.username) like lower(concat('%', cast(:search as string), '%')))")
    List<User> lookupUsers(
        @Param("search") String search,
        @Param("status") UserStatus status
    );

    @Query("SELECT DISTINCT r.code FROM UserRole ur " +
           "JOIN ur.role r " +
           "WHERE ur.user.id = :userId " +
           "AND ur.deletedAt IS NULL " +
           "AND ur.isActive = true " +
           "AND (ur.expiredAt IS NULL OR ur.expiredAt > CURRENT_TIMESTAMP) " +
           "AND r.deletedAt IS NULL " +
           "AND r.isActive = true")
    List<String> findActiveRoleCodesByUserId(@Param("userId") UUID userId);

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
