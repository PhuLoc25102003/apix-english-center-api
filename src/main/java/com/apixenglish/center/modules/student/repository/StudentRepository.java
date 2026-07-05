package com.apixenglish.center.modules.student.repository;

import com.apixenglish.center.modules.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByStudentCodeAndDeletedAtIsNull(String studentCode);

    Optional<Student> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByStudentCodeAndDeletedAtIsNull(String studentCode);
}
