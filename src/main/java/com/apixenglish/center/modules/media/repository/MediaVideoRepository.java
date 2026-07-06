package com.apixenglish.center.modules.media.repository;
import com.apixenglish.center.modules.media.entity.MediaVideo; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
import java.util.*;
public interface MediaVideoRepository extends JpaRepository<MediaVideo,UUID>, JpaSpecificationExecutor<MediaVideo> {
 @Query("select v from MediaVideo v left join fetch v.clazz left join fetch v.student left join fetch v.uploadedBy left join fetch v.approvedBy where v.id=:id and v.deletedAt is null")
 Optional<MediaVideo> findDetail(@Param("id") UUID id);
}
