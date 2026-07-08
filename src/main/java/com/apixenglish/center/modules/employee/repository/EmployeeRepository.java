package com.apixenglish.center.modules.employee.repository;

import com.apixenglish.center.modules.employee.entity.Employee;
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
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByUserIdAndDeletedAtIsNull(UUID userId);

    @Query("SELECT DISTINCT e FROM Employee e " +
           "LEFT JOIN e.user u " +
           "LEFT JOIN e.positions ep " +
           "WHERE e.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) " +
           "  OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) " +
           "  OR (u IS NOT NULL AND (LOWER(u.email) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR LOWER(u.phone) LIKE LOWER(CONCAT('%', cast(:search as string), '%'))))) " +
           "AND (:status IS NULL OR e.employmentStatus = :status) " +
           "AND (:positionId IS NULL OR (ep.position.id = :positionId AND ep.deletedAt IS NULL)) " +
           "AND (:campusId IS NULL OR e.campus.id = :campusId)")
    Page<Employee> searchEmployees(
            @Param("search") String search,
            @Param("status") String status,
            @Param("positionId") UUID positionId,
            @Param("campusId") UUID campusId,
            Pageable pageable);

    @Query("SELECT MAX(e.employeeCode) FROM Employee e WHERE e.employeeCode LIKE 'EMP%'")
    String findMaxEmployeeCode();

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL AND e.employmentStatus = 'ACTIVE'")
    List<Employee> findAllActive();
}
