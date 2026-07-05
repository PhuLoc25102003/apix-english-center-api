package com.apixenglish.center.modules.classmanagement.repository;

import com.apixenglish.center.modules.classmanagement.entity.ClassEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, UUID> {

    Optional<ClassEnrollment> findByEnrollmentCodeAndDeletedAtIsNull(String enrollmentCode);

    boolean existsByStudentIdAndClazzIdAndStatusAndDeletedAtIsNull(UUID studentId, UUID classId, String status);

    long countByClazzIdAndStatusAndDeletedAtIsNull(UUID classId, String status);

    List<ClassEnrollment> findByStudentIdAndDeletedAtIsNull(UUID studentId);

    List<ClassEnrollment> findByClazzIdAndDeletedAtIsNull(UUID classId);

    @Query("SELECT MAX(e.enrollmentCode) FROM ClassEnrollment e WHERE e.enrollmentCode LIKE 'ENR%'")
    String findMaxEnrollmentCode();
}
