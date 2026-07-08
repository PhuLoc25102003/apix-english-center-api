package com.apixenglish.center.modules.attendance.repository;

import com.apixenglish.center.modules.attendance.entity.ClassSessionAttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassSessionAttendanceStatusRepository extends JpaRepository<ClassSessionAttendanceStatus, UUID> {
    Optional<ClassSessionAttendanceStatus> findBySessionIdAndDeletedAtIsNull(UUID sessionId);
}
