package com.apixenglish.center.modules.student.repository;

import com.apixenglish.center.modules.student.entity.Student;
import com.apixenglish.center.modules.student.entity.StudentType;
import com.apixenglish.center.modules.student.entity.StudentAccessMode;
import com.apixenglish.center.modules.student.entity.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByStudentCodeAndDeletedAtIsNull(String studentCode);

    Optional<Student> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByStudentCodeAndDeletedAtIsNull(String studentCode);

    @Query("SELECT s FROM Student s " +
           "WHERE s.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR :search = '' OR " +
           "     LOWER(s.fullName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "     LOWER(s.studentCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))" +
           "AND (:studentType IS NULL OR s.studentType = :studentType)" +
           "AND (:accessMode IS NULL OR s.accessMode = :accessMode)" +
           "AND (:status IS NULL OR s.status = :status)")
    Page<Student> searchStudents(
            @Param("search") String search,
            @Param("studentType") StudentType studentType,
            @Param("accessMode") StudentAccessMode accessMode,
            @Param("status") StudentStatus status,
            Pageable pageable
    );

    @Query("SELECT MAX(s.studentCode) FROM Student s WHERE s.studentCode LIKE 'STU%'")
    String findMaxStudentCode();

    List<Student> findByFullNameAndDateOfBirthAndDeletedAtIsNull(String fullName, LocalDate dateOfBirth);
    List<Student> findByDeletedAtIsNull();
}
