package com.apixenglish.center.modules.classmanagement.repository;

import com.apixenglish.center.modules.classmanagement.entity.ClassStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassStaffRepository extends JpaRepository<ClassStaff, UUID> {

    @Query("select count(cs) > 0 from ClassStaff cs where cs.clazz.id=:classId and cs.employee.id=:employeeId " +
            "and cs.deletedAt is null and cs.startDate<=:date and (cs.endDate is null or cs.endDate>=:date)")
    boolean isAssigned(@Param("employeeId") UUID employeeId, @Param("classId") UUID classId, @Param("date") LocalDate date);

    List<ClassStaff> findByClazzIdAndDeletedAtIsNull(UUID classId);

    List<ClassStaff> findByEmployeeIdAndDeletedAtIsNull(UUID employeeId);

    @Query("SELECT cs FROM ClassStaff cs " +
           "WHERE cs.employee.id = :employeeId " +
           "AND cs.deletedAt IS NULL " +
           "AND cs.startDate <= :date " +
           "AND (cs.endDate IS NULL OR cs.endDate >= :date)")
    List<ClassStaff> findActiveAssignments(@Param("employeeId") UUID employeeId, @Param("date") LocalDate date);

    List<ClassStaff> findByClazzIdAndStaffRoleAndIsPrimaryAndDeletedAtIsNull(UUID classId, String staffRole, Boolean isPrimary);
}
