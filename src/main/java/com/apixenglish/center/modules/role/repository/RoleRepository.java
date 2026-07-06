package com.apixenglish.center.modules.role.repository;

import com.apixenglish.center.modules.role.entity.Role;
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
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    Optional<Role> findByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, UUID id);

    @Query("SELECT r FROM Role r WHERE r.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR lower(r.code) like lower(concat('%', cast(:search as string), '%')) OR lower(r.name) like lower(concat('%', cast(:search as string), '%'))) " +
           "AND (:isActive IS NULL OR r.isActive = :isActive) " +
           "AND (:isSystem IS NULL OR r.isSystem = :isSystem)")
    Page<Role> searchRoles(
        @Param("search") String search, 
        @Param("isActive") Boolean isActive, 
        @Param("isSystem") Boolean isSystem, 
        Pageable pageable
    );

    @Query("SELECT r FROM Role r WHERE r.deletedAt IS NULL " +
           "AND (:isActive = true OR r.isActive = true) " +
           "AND (cast(:search as string) IS NULL OR lower(r.code) like lower(concat('%', cast(:search as string), '%')) OR lower(r.name) like lower(concat('%', cast(:search as string), '%')))")
    List<Role> lookupRoles(
        @Param("search") String search,
        @Param("isActive") boolean isActive
    );
}
