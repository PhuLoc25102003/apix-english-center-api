package com.apixenglish.center.modules.position.repository;

import com.apixenglish.center.modules.position.entity.Position;
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
public interface PositionRepository extends JpaRepository<Position, UUID> {

    Optional<Position> findByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndDeletedAtIsNull(String code);

    @Query("SELECT p FROM Position p " +
           "WHERE p.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR LOWER(p.code) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))" +
           "AND (:isTeachingPosition IS NULL OR p.isTeachingPosition = :isTeachingPosition) " +
           "AND (:isActive IS NULL OR p.isActive = :isActive)")
    Page<Position> searchPositions(
            @Param("search") String search,
            @Param("isTeachingPosition") Boolean isTeachingPosition,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT p FROM Position p WHERE p.deletedAt IS NULL AND p.isActive = true")
    List<Position> findAllActive();
}
