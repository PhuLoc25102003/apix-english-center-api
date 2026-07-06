package com.apixenglish.center.modules.permission.repository;

import com.apixenglish.center.modules.permission.entity.Permission;
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
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    
    Optional<Permission> findByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, UUID id);

    @Query("SELECT p FROM Permission p WHERE p.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR lower(p.code) like lower(concat('%', cast(:search as string), '%')) OR lower(p.description) like lower(concat('%', cast(:search as string), '%'))) " +
           "AND (cast(:module as string) IS NULL OR p.module = :module) " +
           "AND (cast(:action as string) IS NULL OR p.action = :action) " +
           "AND (:isActive IS NULL OR p.isActive = :isActive)")
    Page<Permission> searchPermissions(
        @Param("search") String search,
        @Param("module") String module,
        @Param("action") String action,
        @Param("isActive") Boolean isActive,
        Pageable pageable
    );

    @Query("SELECT p FROM Permission p WHERE p.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR lower(p.code) like lower(concat('%', cast(:search as string), '%')) OR lower(p.description) like lower(concat('%', cast(:search as string), '%'))) " +
           "AND (cast(:module as string) IS NULL OR p.module = :module) " +
           "AND (cast(:action as string) IS NULL OR p.action = :action) " +
           "AND (:isActive IS NULL OR p.isActive = :isActive)")
    List<Permission> lookupPermissions(
        @Param("search") String search,
        @Param("module") String module,
        @Param("action") String action,
        @Param("isActive") Boolean isActive
    );
}
