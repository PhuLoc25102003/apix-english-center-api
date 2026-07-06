package com.apixenglish.center.modules.weeklyupdate.repository;
import com.apixenglish.center.modules.weeklyupdate.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
public interface WeeklyClassUpdateRepository extends JpaRepository<WeeklyClassUpdate,UUID> {
 Page<WeeklyClassUpdate> findByClazzIdAndWeekStartDateGreaterThanEqualAndStatusAndDeletedAtIsNull(UUID classId,LocalDate weekStart,WeeklyUpdateStatus status,Pageable pageable);
 Page<WeeklyClassUpdate> findByClazzIdAndDeletedAtIsNull(UUID classId,Pageable pageable);
}
