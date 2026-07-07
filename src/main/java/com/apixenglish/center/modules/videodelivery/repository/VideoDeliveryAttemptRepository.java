package com.apixenglish.center.modules.videodelivery.repository;
import com.apixenglish.center.modules.videodelivery.entity.VideoDeliveryAttempt; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface VideoDeliveryAttemptRepository extends JpaRepository<VideoDeliveryAttempt,UUID> {
 @Query("select a from VideoDeliveryAttempt a left join fetch a.actorEmployee left join fetch a.parent where a.videoDelivery.id=:id order by a.createdAt") List<VideoDeliveryAttempt> findHistory(@Param("id")UUID id);
}
