package com.apixenglish.center.modules.videodelivery.repository;
import com.apixenglish.center.modules.videodelivery.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface StudentVideoDeliveryRepository extends JpaRepository<StudentVideoDelivery,UUID>,JpaSpecificationExecutor<StudentVideoDelivery> {
 @Query("select d from StudentVideoDelivery d join fetch d.student left join fetch d.clazz left join fetch d.parent left join fetch d.assignedToEmployee left join fetch d.sentByEmployee left join fetch d.batch where d.id=:id and d.deletedAt is null") Optional<StudentVideoDelivery> findDetail(@Param("id")UUID id);
 List<StudentVideoDelivery> findByBatchIdAndDeletedAtIsNull(UUID batchId);
 long countByBatchIdAndStatusAndDeletedAtIsNull(UUID batchId,VideoDeliveryStatus status);
}
