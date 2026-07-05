package com.apixenglish.center.modules.campus.repository;

import com.apixenglish.center.modules.campus.entity.Campus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampusRepository extends JpaRepository<Campus, UUID> {

    Optional<Campus> findByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndDeletedAtIsNull(String code);

    @Query("SELECT c FROM Campus c " +
           "WHERE c.deletedAt IS NULL " +
           "AND (:search IS NULL OR " +
           "     LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Campus> searchCampuses(@Param("search") String search, Pageable pageable);
}
