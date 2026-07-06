package com.apixenglish.center.modules.media.repository;
import com.apixenglish.center.modules.media.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
import java.util.*;
public interface VideoShareLinkRepository extends JpaRepository<VideoShareLink,UUID> {
 @Query("select s from VideoShareLink s join fetch s.video v left join fetch v.student where s.shareToken=:token and s.deletedAt is null") Optional<VideoShareLink> findByToken(@Param("token") String token);
 List<VideoShareLink> findByVideoIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID videoId);
 @Query("select s from VideoShareLink s where s.video.id=:videoId and s.createdForParent.id=:parentId and s.status='ACTIVE' and s.deletedAt is null and (s.expiresAt is null or s.expiresAt>:now) order by s.createdAt desc")
 List<VideoShareLink> findActive(@Param("videoId") UUID videoId,@Param("parentId") UUID parentId,@Param("now") java.time.Instant now);
}
