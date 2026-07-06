package com.apixenglish.center.modules.score.repository;
import com.apixenglish.center.modules.score.entity.*;import org.springframework.data.domain.*;import org.springframework.data.jpa.repository.JpaRepository;import java.util.UUID;
public interface ScoreItemRepository extends JpaRepository<ScoreItem,UUID>{Page<ScoreItem> findByClazzIdAndDeletedAtIsNull(UUID classId,Pageable pageable);}
