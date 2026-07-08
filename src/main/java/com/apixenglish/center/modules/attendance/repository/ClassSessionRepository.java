package com.apixenglish.center.modules.attendance.repository;

import com.apixenglish.center.modules.attendance.entity.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClassSessionRepository extends JpaRepository<ClassSession, UUID> {

    @Query("SELECT cs FROM ClassSession cs " +
           "LEFT JOIN ClassSessionAttendanceStatus as_status ON as_status.session.id = cs.id " +
           "WHERE cs.deletedAt IS NULL " +
           "AND (:classId IS NULL OR cs.clazz.id = :classId) " +
           "AND (cast(:fromDate as date) IS NULL OR cs.sessionDate >= :fromDate) " +
           "AND (cast(:toDate as date) IS NULL OR cs.sessionDate <= :toDate) " +
           "AND (cast(:status as string) IS NULL OR cs.status = :status) " +
           "AND (cast(:attendanceStatus as string) IS NULL OR as_status.status = :attendanceStatus) " +
           "AND (:teacherId IS NULL OR EXISTS (SELECT cst FROM ClassStaff cst WHERE cst.clazz.id = cs.clazz.id AND cst.employee.id = :teacherId AND cst.deletedAt IS NULL))")
    List<ClassSession> searchSessions(
            @Param("classId") UUID classId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status,
            @Param("attendanceStatus") String attendanceStatus,
            @Param("teacherId") UUID teacherId);

    List<ClassSession> findByClazzIdAndDeletedAtIsNull(UUID classId);

    @Query("SELECT cs FROM ClassSession cs WHERE cs.clazz.id = :classId AND cs.sessionDate >= :date AND cs.deletedAt IS NULL")
    List<ClassSession> findFutureSessions(@Param("classId") UUID classId, @Param("date") LocalDate date);
}
