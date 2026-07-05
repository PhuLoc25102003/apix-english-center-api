package com.apixenglish.center.modules.parent.repository;

import com.apixenglish.center.modules.parent.entity.Parent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParentRepository extends JpaRepository<Parent, UUID> {

    Optional<Parent> findByParentCodeAndDeletedAtIsNull(String parentCode);

    Optional<Parent> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByParentCodeAndDeletedAtIsNull(String parentCode);

    @Query("SELECT p FROM Parent p " +
           "WHERE p.deletedAt IS NULL " +
           "AND (:search IS NULL OR " +
           "     LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(p.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(p.parentCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Parent> searchParents(@Param("search") String search, Pageable pageable);

    @Query("SELECT MAX(p.parentCode) FROM Parent p WHERE p.parentCode LIKE 'PAR%'")
    String findMaxParentCode();
}
