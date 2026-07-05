package com.apixenglish.center.modules.student.repository;

import com.apixenglish.center.modules.student.entity.StudentParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentParentRepository extends JpaRepository<StudentParent, UUID> {

    @Query("SELECT sp FROM StudentParent sp JOIN FETCH sp.parent JOIN FETCH sp.student " +
           "WHERE sp.student.id = :studentId AND sp.parent.id = :parentId AND sp.deletedAt IS NULL")
    Optional<StudentParent> findByStudentIdAndParentIdAndDeletedAtIsNull(@Param("studentId") UUID studentId, @Param("parentId") UUID parentId);

    @Query("SELECT sp FROM StudentParent sp WHERE sp.student.id = :studentId AND sp.parent.id = :parentId")
    Optional<StudentParent> findByStudentIdAndParentId(@Param("studentId") UUID studentId, @Param("parentId") UUID parentId);

    @Query("SELECT sp FROM StudentParent sp JOIN FETCH sp.parent WHERE sp.student.id = :studentId AND sp.deletedAt IS NULL")
    List<StudentParent> findByStudentIdAndDeletedAtIsNull(@Param("studentId") UUID studentId);

    @Query("SELECT sp FROM StudentParent sp JOIN FETCH sp.student WHERE sp.parent.id = :parentId AND sp.deletedAt IS NULL")
    List<StudentParent> findByParentIdAndDeletedAtIsNull(@Param("parentId") UUID parentId);
}
