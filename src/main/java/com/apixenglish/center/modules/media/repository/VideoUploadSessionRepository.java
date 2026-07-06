package com.apixenglish.center.modules.media.repository;
import com.apixenglish.center.modules.media.entity.VideoUploadSession; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
import java.util.*;
public interface VideoUploadSessionRepository extends JpaRepository<VideoUploadSession,UUID> {
 @Query("select s from VideoUploadSession s left join fetch s.clazz left join fetch s.student join fetch s.requestedBy where s.uploadToken=:token and s.deletedAt is null")
 Optional<VideoUploadSession> findByToken(@Param("token") String token);
}
