package com.apixenglish.center.modules.student.repository;

import com.apixenglish.center.modules.student.entity.StudentParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentParentRepository extends JpaRepository<StudentParent, UUID> {

    Optional<StudentParent> findByStudentIdAndParentIdAndDeletedAtIsNull(UUID studentId, UUID parentId);

    List<StudentParent> findByStudentIdAndDeletedAtIsNull(UUID studentId);
}
