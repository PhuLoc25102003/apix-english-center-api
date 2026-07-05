package com.apixenglish.center.modules.classmanagement.repository;

import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClazzRepository extends JpaRepository<Clazz, UUID> {

    Optional<Clazz> findByClassCodeAndDeletedAtIsNull(String classCode);

    boolean existsByClassCodeAndDeletedAtIsNull(String classCode);

    @Query("SELECT c FROM Clazz c " +
           "WHERE c.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR " +
           "     LOWER(c.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "     LOWER(c.classCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<Clazz> searchClasses(@Param("search") String search, Pageable pageable);

    @Query("SELECT MAX(c.classCode) FROM Clazz c WHERE c.classCode LIKE 'CLS%'")
    String findMaxClassCode();
}
