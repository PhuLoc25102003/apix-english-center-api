package com.apixenglish.center.modules.tuition.repository;

import com.apixenglish.center.modules.tuition.entity.TuitionPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TuitionPackageRepository extends JpaRepository<TuitionPackage, UUID> {
    List<TuitionPackage> findByIsActiveTrueAndDeletedAtIsNullOrderByNumberOfMonthsAsc();
}
