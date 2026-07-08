package com.apixenglish.center.modules.classmanagement.repository;

import com.apixenglish.center.modules.classmanagement.entity.Clazz;
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
public interface ClazzRepository extends JpaRepository<Clazz, UUID> {

    Optional<Clazz> findByClassCodeAndDeletedAtIsNull(String classCode);

    boolean existsByClassCodeAndDeletedAtIsNull(String classCode);

    @Query("SELECT DISTINCT c FROM Clazz c " +
           "LEFT JOIN ClassStaff cs ON cs.clazz.id = c.id AND cs.deletedAt IS NULL " +
           "WHERE c.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR " +
           "     LOWER(c.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "     LOWER(c.classCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))" +
           "AND (:teacherId IS NULL OR cs.employee.id = :teacherId)")
    Page<Clazz> searchClasses(@Param("search") String search, @Param("teacherId") UUID teacherId, Pageable pageable);

    @Query("SELECT MAX(c.classCode) FROM Clazz c WHERE c.classCode LIKE 'CLS%'")
    String findMaxClassCode();
    List<Clazz> findByDeletedAtIsNull();
}
