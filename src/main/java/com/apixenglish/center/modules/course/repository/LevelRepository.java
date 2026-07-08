package com.apixenglish.center.modules.course.repository;

import com.apixenglish.center.modules.course.entity.Level;
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
public interface LevelRepository extends JpaRepository<Level, UUID> {

    Optional<Level> findByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndDeletedAtIsNull(String code);
    List<Level> findByDeletedAtIsNullAndIsActiveTrueOrderByOrderIndexAsc();

    @Query("SELECT l FROM Level l " +
           "WHERE l.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR " +
           "     LOWER(l.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "     LOWER(l.code) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<Level> searchLevels(@Param("search") String search, Pageable pageable);
}
