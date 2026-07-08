package com.apixenglish.center.modules.curriculum.repository;

import com.apixenglish.center.modules.curriculum.entity.Curriculum;
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
public interface CurriculumRepository extends JpaRepository<Curriculum, UUID> {

    @Query("SELECT c FROM Curriculum c " +
           "JOIN FETCH c.course co " +
           "LEFT JOIN FETCH co.level l " +
           "WHERE c.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR LOWER(co.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))" +
           "AND (:courseId IS NULL OR co.id = :courseId) " +
           "AND (:isActive IS NULL OR c.isActive = :isActive)")
    Page<Curriculum> searchCurriculums(
            @Param("search") String search,
            @Param("courseId") UUID courseId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT c FROM Curriculum c WHERE c.deletedAt IS NULL AND c.isActive = true")
    List<Curriculum> findAllActive();

    boolean existsByCourseIdAndVersionNameAndDeletedAtIsNull(UUID courseId, String versionName);
}
