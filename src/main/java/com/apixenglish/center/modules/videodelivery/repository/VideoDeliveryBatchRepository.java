package com.apixenglish.center.modules.videodelivery.repository;
import com.apixenglish.center.modules.videodelivery.entity.VideoDeliveryBatch; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface VideoDeliveryBatchRepository extends JpaRepository<VideoDeliveryBatch,UUID>,JpaSpecificationExecutor<VideoDeliveryBatch> {
 @Query("select b from VideoDeliveryBatch b left join fetch b.clazz left join fetch b.createdByEmployee where b.id=:id and b.deletedAt is null") Optional<VideoDeliveryBatch> findDetail(@Param("id")UUID id);
}
