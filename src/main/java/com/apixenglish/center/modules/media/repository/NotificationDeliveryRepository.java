package com.apixenglish.center.modules.media.repository;
import com.apixenglish.center.modules.media.entity.NotificationDelivery; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery,UUID> {
 List<NotificationDelivery> findByEntityTypeAndEntityIdAndDeletedAtIsNullOrderByCreatedAtDesc(String entityType,UUID entityId);
}
