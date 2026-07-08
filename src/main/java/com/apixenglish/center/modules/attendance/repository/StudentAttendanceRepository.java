package com.apixenglish.center.modules.attendance.repository;

import com.apixenglish.center.modules.attendance.entity.StudentAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentAttendanceRepository extends JpaRepository<StudentAttendance, UUID> {

    List<StudentAttendance> findBySessionIdAndDeletedAtIsNull(UUID sessionId);

    Optional<StudentAttendance> findBySessionIdAndStudentIdAndDeletedAtIsNull(UUID sessionId, UUID studentId);

    boolean existsBySessionIdAndStudentIdAndDeletedAtIsNull(UUID sessionId, UUID studentId);

    @Query("SELECT sa FROM StudentAttendance sa " +
           "JOIN FETCH sa.session s " +
           "WHERE sa.student.id = :studentId " +
           "AND s.clazz.id = :classId " +
           "AND sa.deletedAt IS NULL")
    List<StudentAttendance> findByStudentAndClass(
            @Param("studentId") UUID studentId,
            @Param("classId") UUID classId);

    @Query("SELECT sa FROM StudentAttendance sa " +
           "JOIN FETCH sa.session s " +
           "WHERE s.clazz.id = :classId " +
           "AND s.sessionDate >= :date " +
           "AND sa.student.id = :studentId " +
           "AND sa.deletedAt IS NULL")
    List<StudentAttendance> findFutureAttendance(
            @Param("classId") UUID classId,
            @Param("studentId") UUID studentId,
            @Param("date") LocalDate date);
}
