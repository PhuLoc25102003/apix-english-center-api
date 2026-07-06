package com.apixenglish.center.modules.media.dto;
import com.apixenglish.center.modules.media.entity.*; import jakarta.validation.constraints.*;
import java.time.*; import java.util.*;
public final class VideoDtos { private VideoDtos(){}
 public record CreateUploadSession(UUID classId,UUID studentId,UUID sessionId,@NotNull VideoType videoType,LocalDate targetMonth,@Size(max=255) String title,String description){}
 public record UploadSessionCreated(UUID uploadSessionId,String uploadToken,String uploadPageUrl,String qrPayload,Instant expiresAt,VideoUploadStatus status){}
 public record UploadSessionPublic(UUID uploadSessionId,String className,String studentName,VideoType videoType,LocalDate targetMonth,String title,String description,Instant expiresAt,VideoUploadStatus status,long maxFileSizeMb,List<String> allowedMimeTypes){}
 public record PresignRequest(@NotBlank @Size(max=255) String fileName,@NotBlank String mimeType,@Positive long fileSizeBytes){}
 public record PresignResponse(String uploadUrl,String method,Map<String,String> headers,String storageBucket,String storageKey,Instant expiresAt){}
 public record CompleteRequest(@NotBlank String storageBucket,@NotBlank String storageKey,@NotBlank @Size(max=255) String originalFileName,@NotBlank String mimeType,@Positive long fileSizeBytes,@PositiveOrZero Integer durationSeconds,String checksum){}
 public record CompleteResponse(UUID videoId,MediaVideoStatus status,String title,VideoType videoType,String studentName,String className,Instant createdAt){}
 public record VideoListItem(UUID id,String title,VideoType videoType,UUID studentId,String studentName,UUID classId,String className,LocalDate targetMonth,MediaVideoStatus status,String originalFileName,long fileSizeBytes,Integer durationSeconds,String uploadedByName,String approvedByName,Instant approvedAt,Instant deliveredAt,boolean parentVisible,Instant createdAt,Instant updatedAt){}
 public record ShareSummary(UUID id,ShareLinkStatus status,Instant expiresAt,Integer accessCount,UUID parentId){}
 public record DeliverySummary(UUID id,DeliveryStatus status,UUID parentId,Instant sentAt){}
 public record VideoDetail(UUID id,UUID uploadSessionId,UUID classId,String className,UUID studentId,String studentName,UUID sessionId,VideoType videoType,LocalDate targetMonth,String title,String description,String originalFileName,String mimeType,long fileSizeBytes,Integer durationSeconds,String checksum,MediaVideoStatus status,String uploadedByName,String approvedByName,Instant approvedAt,String rejectionReason,Instant deliveredAt,boolean parentVisible,String playbackUrl,List<ShareSummary> shareLinks,List<DeliverySummary> deliveries,Instant createdAt,Instant updatedAt,Integer version){}
 public record ApprovalRequest(String note,Boolean parentVisible){}
 public record RejectRequest(@NotBlank String reason){}
 public record ShareLinkRequest(UUID parentId,Instant expiresAt,@Positive Integer expiryDays){}
 public record ShareLinkResponse(UUID shareLinkId,String shareUrl,String shareToken,Instant expiresAt,ShareLinkStatus status){}
 public record PublicVideo(String title,String description,String studentDisplayName,VideoType videoType,String playbackUrl,Instant expiresAt){}
 public record ManualZaloRequest(@NotNull UUID parentId){}
 public record ManualZaloResponse(UUID deliveryId,String parentName,String parentPhone,String zaloOpenUrl,String messageContent,String shareUrl,DeliveryStatus status){}
 public record DeliveryStatusRequest(@NotNull DeliveryStatus status,String note){}
 public record DeliveryResponse(UUID id,DeliveryStatus status,Instant sentAt,String note){}
}
