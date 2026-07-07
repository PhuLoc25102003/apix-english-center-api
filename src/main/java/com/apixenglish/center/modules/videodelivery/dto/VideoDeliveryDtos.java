package com.apixenglish.center.modules.videodelivery.dto;
import com.apixenglish.center.modules.media.entity.VideoType; import com.apixenglish.center.modules.videodelivery.entity.*;
import jakarta.validation.constraints.*; import java.time.*; import java.util.*;
public final class VideoDeliveryDtos { private VideoDeliveryDtos(){}
 public record CreateBatch(UUID classId,@NotNull VideoType videoType,LocalDate targetMonth,@NotBlank @Size(max=255)String title,String description,LocalDate dueDate,List<UUID> studentIds){}
 public record UpdateBatch(@NotBlank @Size(max=255)String title,String description,LocalDate dueDate,@NotNull VideoBatchStatus status,String note){}
 public record BatchItem(UUID id,UUID classId,String className,VideoType videoType,LocalDate targetMonth,String title,String description,LocalDate dueDate,VideoBatchStatus status,long totalStudents,long pendingCount,long sentCount,long failedCount,long skippedCount,Instant completedAt,Instant createdAt,Instant updatedAt){}
 public record DeliveryItem(UUID id,UUID batchId,UUID studentId,String studentCode,String studentFullName,UUID classId,String className,UUID parentId,String parentName,String parentPhone,VideoType videoType,LocalDate targetMonth,String title,String messageContent,VideoDeliveryChannel channel,VideoDeliveryStatus status,String assignedToEmployeeName,String sentByEmployeeName,Instant sentAt,String failedReason,String skippedReason,String note,Instant createdAt,Instant updatedAt){}
 public record StudentSummary(UUID id,String code,String fullName){}
 public record ClassSummary(UUID id,String name){}
 public record ParentSummary(UUID id,String name,String phone){}
 public record AttemptItem(UUID id,VideoAttemptAction action,UUID actorEmployeeId,String actorEmployeeName,UUID parentId,String recipientPhone,String messageContent,String note,Instant createdAt){}
 public record DeliveryDetail(DeliveryItem delivery,StudentSummary student,ClassSummary clazz,ParentSummary parent,List<AttemptItem> attempts,Integer version){}
 public record PrepareMessageResponse(UUID deliveryId,String parentName,String parentPhone,String zaloOpenUrl,String messageContent,VideoDeliveryStatus status){}
 public record NoteRequest(String note){}
 public record FailedRequest(@NotBlank String failedReason){}
 public record SkippedRequest(@NotBlank String skippedReason){}
 public record Stats(long totalDeliveries,long pendingCount,long preparedCount,long openedZaloCount,long sentCount,long failedCount,long skippedCount,double completionRate,long overdueCount){}
}
