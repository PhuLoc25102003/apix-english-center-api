package com.apixenglish.center.modules.classmanagement.repository;

import com.apixenglish.center.modules.classmanagement.entity.ClassStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.UUID;

public interface ClassStaffRepository extends JpaRepository<ClassStaff, UUID> {
    @Query("select count(cs) > 0 from ClassStaff cs where cs.clazz.id=:classId and cs.employee.id=:employeeId " +
            "and cs.deletedAt is null and cs.startDate<=:date and (cs.endDate is null or cs.endDate>=:date)")
    boolean isAssigned(@Param("employeeId") UUID employeeId, @Param("classId") UUID classId, @Param("date") LocalDate date);
}
