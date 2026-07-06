package com.apixenglish.center.modules.classmanagement.repository;

import com.apixenglish.center.modules.classmanagement.entity.ClassEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, UUID> {

    Optional<ClassEnrollment> findByEnrollmentCodeAndDeletedAtIsNull(String enrollmentCode);

    boolean existsByStudentIdAndClazzIdAndStatusAndDeletedAtIsNull(UUID studentId, UUID classId, String status);

    long countByClazzIdAndStatusAndDeletedAtIsNull(UUID classId, String status);

    @Query("select count(e) from ClassEnrollment e where e.clazz.id=:classId and e.deletedAt is null and e.status in ('TRIAL','ACTIVE','FROZEN')")
    long countCapacityOccupying(@Param("classId") UUID classId);

    @Query("select e from ClassEnrollment e join fetch e.student s join fetch e.clazz c left join fetch c.course co left join fetch co.level left join fetch c.campus " +
            "where e.deletedAt is null and (:search is null or lower(e.enrollmentCode) like lower(concat('%',cast(:search as string),'%')) or lower(s.fullName) like lower(concat('%',cast(:search as string),'%')) or lower(s.studentCode) like lower(concat('%',cast(:search as string),'%')) or lower(c.name) like lower(concat('%',cast(:search as string),'%'))) " +
            "and (:status is null or e.status=:status) and (:classId is null or c.id=:classId) and (:studentId is null or s.id=:studentId) and (:campusId is null or c.campus.id=:campusId) and (:source is null or e.source=:source) and (:enrolledFrom is null or e.enrolledDate>=:enrolledFrom) and (:enrolledTo is null or e.enrolledDate<=:enrolledTo)")
    Page<ClassEnrollment> search(@Param("search") String search,@Param("status") String status,@Param("classId") UUID classId,@Param("studentId") UUID studentId,@Param("campusId") UUID campusId,@Param("source") String source,@Param("enrolledFrom") LocalDate enrolledFrom,@Param("enrolledTo") LocalDate enrolledTo,Pageable pageable);

    List<ClassEnrollment> findByStudentIdAndDeletedAtIsNull(UUID studentId);

    List<ClassEnrollment> findByClazzIdAndDeletedAtIsNull(UUID classId);

    @Query("SELECT MAX(e.enrollmentCode) FROM ClassEnrollment e WHERE e.enrollmentCode LIKE 'ENR%'")
    String findMaxEnrollmentCode();
}
