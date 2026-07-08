package com.apixenglish.center.modules.schedule.repository;

import com.apixenglish.center.modules.schedule.entity.ClassSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, UUID> {

    @Query("SELECT cs FROM ClassSchedule cs " +
           "JOIN FETCH cs.clazz c " +
           "JOIN FETCH cs.room r " +
           "JOIN FETCH r.campus ca " +
           "WHERE cs.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR LOWER(r.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))" +
           "AND (:classId IS NULL OR c.id = :classId) " +
           "AND (:roomId IS NULL OR r.id = :roomId) " +
           "AND (:campusId IS NULL OR ca.id = :campusId) " +
           "AND (:patternCode IS NULL OR cs.patternCode = :patternCode) " +
           "AND (:status IS NULL OR cs.status = :status) " +
           "AND (:teacherId IS NULL OR EXISTS (SELECT csf FROM ClassStaff csf WHERE csf.clazz.id = c.id AND csf.employee.id = :teacherId AND csf.deletedAt IS NULL))")
    Page<ClassSchedule> searchSchedules(
            @Param("search") String search,
            @Param("classId") UUID classId,
            @Param("roomId") UUID roomId,
            @Param("campusId") UUID campusId,
            @Param("patternCode") String patternCode,
            @Param("status") String status,
            @Param("teacherId") UUID teacherId,
            Pageable pageable);

    List<ClassSchedule> findByClazzIdAndDeletedAtIsNull(UUID classId);
    List<ClassSchedule> findByDeletedAtIsNullAndStatus(String status);
}
