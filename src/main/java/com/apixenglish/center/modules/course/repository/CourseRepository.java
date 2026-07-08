package com.apixenglish.center.modules.course.repository;

import com.apixenglish.center.modules.course.entity.Course;
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
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndDeletedAtIsNull(String code);
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.level WHERE c.deletedAt IS NULL ORDER BY c.name")
    List<Course> findForLookup();

    @Query("SELECT c FROM Course c " +
           "WHERE c.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR " +
           "     LOWER(c.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "     LOWER(c.code) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<Course> searchCourses(@Param("search") String search, Pageable pageable);
}
