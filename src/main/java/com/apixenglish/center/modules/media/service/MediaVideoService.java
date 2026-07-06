package com.apixenglish.center.modules.media.service;

import com.apixenglish.center.common.exception.*;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.config.MediaProperties;
import com.apixenglish.center.modules.audit.service.AuditService;
import com.apixenglish.center.modules.classmanagement.entity.*;
import com.apixenglish.center.modules.classmanagement.repository.*;
import com.apixenglish.center.modules.classmanagement.service.ClassScopeGuard;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.media.dto.VideoDtos.*;
import com.apixenglish.center.modules.media.entity.*;
import com.apixenglish.center.modules.media.repository.*;
import com.apixenglish.center.modules.media.storage.ObjectStorageService;
import com.apixenglish.center.modules.parent.entity.Parent;
import com.apixenglish.center.modules.parent.repository.ParentRepository;
import com.apixenglish.center.modules.student.entity.Student;
import com.apixenglish.center.modules.student.repository.*;
import com.apixenglish.center.security.CurrentActor;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class MediaVideoService {
 private static final String MODULE="MEDIA_VIDEO"; private static final SecureRandom RANDOM=new SecureRandom();
 private final VideoUploadSessionRepository sessions; private final MediaVideoRepository videos;
 private final VideoShareLinkRepository shares; private final NotificationDeliveryRepository deliveries;
 private final ClazzRepository classes; private final StudentRepository students; private final ParentRepository parents;
 private final StudentParentRepository studentParents; private final ClassEnrollmentRepository enrollments;
 private final ClassScopeGuard scope; private final CurrentActor actor; private final JdbcTemplate jdbc;
 private final MediaProperties media; private final ObjectStorageService storage; private final AuditService audit;

 @Transactional public UploadSessionCreated createSession(CreateUploadSession request){
  validateTarget(request.videoType(),request.classId(),request.studentId(),request.targetMonth());
  Clazz clazz=request.classId()==null?null:classes.findById(request.classId()).filter(c->c.getDeletedAt()==null).orElseThrow(()->new ResourceNotFoundException("Class not found"));
  Student student=request.studentId()==null?null:students.findById(request.studentId()).filter(s->s.getDeletedAt()==null).orElseThrow(()->new ResourceNotFoundException("Student not found"));
  if(clazz!=null&&student!=null&&!isEnrolled(student.getId(),clazz.getId())) throw new BusinessException("Student is not enrolled in the selected class","VIDEO_TARGET_MISMATCH");
  if(request.sessionId()!=null){
   UUID sessionClass=jdbc.query("select class_id from class_sessions where id=? and deleted_at is null",rs->rs.next()?(UUID)rs.getObject(1):null,request.sessionId());
   if(sessionClass==null) throw new ResourceNotFoundException("Class session not found");
   if(clazz==null||!clazz.getId().equals(sessionClass)) throw new BusinessException("Class session does not belong to selected class","VIDEO_SESSION_CLASS_MISMATCH");
  }
  enforceCreateScope(clazz);
  Employee employee=scope.currentEmployee(); String token=token(); Instant expires=Instant.now().plus(Duration.ofMinutes(media.getUploadSessionExpiryMinutes()));
  VideoUploadSession value=VideoUploadSession.builder().uploadToken(token).clazz(clazz).student(student).sessionId(request.sessionId())
    .videoType(request.videoType()).targetMonth(request.targetMonth()).title(request.title()).description(request.description())
    .requestedBy(employee).status(VideoUploadStatus.PENDING).expiresAt(expires).build();
  value=sessions.save(value); audit.record("UPLOAD_SESSION_CREATED",MODULE,"VideoUploadSession",value.getId(),null,Map.of("status","PENDING","videoType",value.getVideoType().name()));
  String url=join(media.getUploadPageBaseUrl(),token); return new UploadSessionCreated(value.getId(),token,url,url,expires,value.getStatus());
 }

 @Transactional public UploadSessionPublic getUploadSession(String token){
  VideoUploadSession s=activeSession(token,false);
  return new UploadSessionPublic(s.getId(),name(s.getClazz()),name(s.getStudent()),s.getVideoType(),s.getTargetMonth(),s.getTitle(),s.getDescription(),s.getExpiresAt(),s.getStatus(),media.getVideo().getMaxFileSizeMb(),List.copyOf(media.getVideo().getAllowedMimeTypes()));
 }

 @Transactional public PresignResponse presign(String token,PresignRequest request){
  VideoUploadSession s=activeSession(token,true); validateFile(request.mimeType(),request.fileSizeBytes());
  String fileName=safeFileName(request.fileName()); String key="videos/"+s.getId()+"/"+UUID.randomUUID()+"-"+fileName;
  var signed=storage.presignUpload(key,request.mimeType(),request.fileSizeBytes());
  s.setStatus(VideoUploadStatus.UPLOADING); s.setExpectedStorageBucket(storage.bucket()); s.setExpectedStorageKey(key);
  s.setExpectedFileName(fileName); s.setExpectedMimeType(request.mimeType()); s.setExpectedFileSizeBytes(request.fileSizeBytes()); sessions.save(s);
  auditSession(s,"PRESIGNED_URL_REQUESTED",Map.of("storageKey",key,"mimeType",request.mimeType(),"fileSizeBytes",request.fileSizeBytes()));
  return new PresignResponse(signed.url(),"PUT",Map.of("Content-Type",request.mimeType()),storage.bucket(),key,signed.expiresAt());
 }

 @Transactional public CompleteResponse complete(String token,CompleteRequest request){
  VideoUploadSession s=activeSession(token,true); if(s.getStatus()!=VideoUploadStatus.UPLOADING) throw new ConflictException("Upload session has no active presigned upload","UPLOAD_NOT_STARTED");
  if(!Objects.equals(s.getExpectedStorageBucket(),request.storageBucket())||!Objects.equals(s.getExpectedStorageKey(),request.storageKey())
    ||!Objects.equals(s.getExpectedFileName(),safeFileName(request.originalFileName()))||!Objects.equals(s.getExpectedMimeType(),request.mimeType())
    ||!Objects.equals(s.getExpectedFileSizeBytes(),request.fileSizeBytes())) throw new BusinessException("Completion metadata does not match this upload session","UPLOAD_CLAIM_MISMATCH");
  storage.verifyObject(request.storageBucket(),request.storageKey(),request.mimeType(),request.fileSizeBytes());
  MediaVideo video=MediaVideo.builder().uploadSession(s).clazz(s.getClazz()).student(s.getStudent()).sessionId(s.getSessionId()).videoType(s.getVideoType())
    .targetMonth(s.getTargetMonth()).title(StringUtils.hasText(s.getTitle())?s.getTitle():defaultTitle(s)).description(s.getDescription())
    .originalFileName(request.originalFileName()).storageBucket(request.storageBucket()).storageKey(request.storageKey()).mimeType(request.mimeType())
    .fileSizeBytes(request.fileSizeBytes()).durationSeconds(request.durationSeconds()).checksum(request.checksum()).status(MediaVideoStatus.READY)
    .uploadedBy(s.getRequestedBy()).parentVisible(false).build();
  video=videos.save(video); s.setStatus(VideoUploadStatus.COMPLETED);s.setCompletedAt(Instant.now());sessions.save(s);
  auditSession(s,"UPLOAD_COMPLETED",Map.of("videoId",video.getId().toString(),"status",video.getStatus().name()));
  return completeResponse(video);
 }

 @Transactional public void cancelSession(UUID id){
  VideoUploadSession s=sessions.findById(id).filter(x->x.getDeletedAt()==null).orElseThrow(()->new ResourceNotFoundException("Upload session not found"));
  Employee current=scope.currentEmployee(); if(!current.getId().equals(s.getRequestedBy().getId())&&!actor.has("media-video:approve")) throw new ForbiddenException("Only the creator or an authorized reviewer can cancel this session");
  if(EnumSet.of(VideoUploadStatus.COMPLETED,VideoUploadStatus.CANCELLED,VideoUploadStatus.EXPIRED).contains(s.getStatus())) throw new ConflictException("Upload session cannot be cancelled from "+s.getStatus());
  s.setStatus(VideoUploadStatus.CANCELLED);s.setCancelledAt(Instant.now());sessions.save(s);audit.record("UPLOAD_SESSION_CANCELLED",MODULE,"VideoUploadSession",id,null,Map.of("status","CANCELLED"));
 }

 @Transactional(readOnly=true) public PageResponse<VideoListItem> list(String search,UUID classId,UUID studentId,VideoType type,LocalDate month,MediaVideoStatus status,UUID uploadedBy,Boolean parentVisible,Pageable pageable){
  Specification<MediaVideo> spec=(root,q,cb)->{
   List<Predicate> p=new ArrayList<>();p.add(cb.isNull(root.get("deletedAt")));
   if(StringUtils.hasText(search)){String like="%"+search.toLowerCase(Locale.ROOT)+"%";p.add(cb.or(cb.like(cb.lower(root.get("title")),like),cb.like(cb.lower(root.get("originalFileName")),like),cb.like(cb.lower(root.join("student",JoinType.LEFT).get("fullName")),like),cb.like(cb.lower(root.join("clazz",JoinType.LEFT).get("name")),like)));}
   if(classId!=null)p.add(cb.equal(root.get("clazz").get("id"),classId));if(studentId!=null)p.add(cb.equal(root.get("student").get("id"),studentId));if(type!=null)p.add(cb.equal(root.get("videoType"),type));if(month!=null)p.add(cb.equal(root.get("targetMonth"),month));if(status!=null)p.add(cb.equal(root.get("status"),status));if(uploadedBy!=null)p.add(cb.equal(root.get("uploadedBy").get("id"),uploadedBy));if(parentVisible!=null)p.add(cb.equal(root.get("parentVisible"),parentVisible));
   if(isClassScoped()){Employee e=scope.currentEmployee();Subquery<UUID> sq=q.subquery(UUID.class);Root<ClassStaff> cs=sq.from(ClassStaff.class);sq.select(cs.get("clazz").get("id")).where(cb.equal(cs.get("employee").get("id"),e.getId()),cb.isNull(cs.get("deletedAt")),cb.lessThanOrEqualTo(cs.get("startDate"),LocalDate.now()),cb.or(cb.isNull(cs.get("endDate")),cb.greaterThanOrEqualTo(cs.get("endDate"),LocalDate.now())));p.add(root.get("clazz").get("id").in(sq));}
   return cb.and(p.toArray(Predicate[]::new));};
  return PageResponse.of(videos.findAll(spec,pageable).map(this::listItem));
 }

 @Transactional(readOnly=true) public VideoDetail detail(UUID id){MediaVideo v=findVideo(id);enforceReadScope(v);return detail(v);}
 @Transactional public VideoDetail approve(UUID id,ApprovalRequest request){MediaVideo v=findVideo(id);requireStatus(v,MediaVideoStatus.READY,MediaVideoStatus.UPLOADED,MediaVideoStatus.REJECTED);Employee e=scope.currentEmployee();v.setStatus(MediaVideoStatus.APPROVED);v.setApprovedBy(e);v.setApprovedAt(Instant.now());v.setRejectedBy(null);v.setRejectedAt(null);v.setRejectionReason(null);v.setParentVisible(request!=null&&Boolean.TRUE.equals(request.parentVisible()));videos.save(v);Map<String,Object> after=new LinkedHashMap<>();after.put("status","APPROVED");if(request!=null&&StringUtils.hasText(request.note()))after.put("note",request.note());audit.record("VIDEO_APPROVED",MODULE,"MediaVideo",id,null,after);return detail(v);}
 @Transactional public VideoDetail reject(UUID id,String reason){MediaVideo v=findVideo(id);requireStatus(v,MediaVideoStatus.READY,MediaVideoStatus.UPLOADED,MediaVideoStatus.APPROVED);Employee e=scope.currentEmployee();v.setStatus(MediaVideoStatus.REJECTED);v.setRejectedBy(e);v.setRejectedAt(Instant.now());v.setRejectionReason(reason);v.setParentVisible(false);videos.save(v);audit.record("VIDEO_REJECTED",MODULE,"MediaVideo",id,null,Map.of("status","REJECTED","reason",reason));return detail(v);}

 @Transactional public ShareLinkResponse createShare(UUID id,ShareLinkRequest request){MediaVideo v=findVideo(id);if(v.getStatus()!=MediaVideoStatus.APPROVED&&v.getStatus()!=MediaVideoStatus.DELIVERED)throw new ConflictException("Video must be approved before sharing","VIDEO_NOT_APPROVED");Parent parent=null;if(request!=null&&request.parentId()!=null){parent=findParent(request.parentId());validateParent(v,parent);}
  Instant expiry=request!=null&&request.expiresAt()!=null?request.expiresAt():Instant.now().plus(Duration.ofDays(request!=null&&request.expiryDays()!=null?request.expiryDays():media.getShareLinkExpiryDays()));if(!expiry.isAfter(Instant.now()))throw new BusinessException("Share link expiry must be in the future","INVALID_SHARE_EXPIRY");
  v.setParentVisible(true); videos.save(v); VideoShareLink s=VideoShareLink.builder().video(v).shareToken(token()).status(ShareLinkStatus.ACTIVE).expiresAt(expiry).createdForParent(parent).createdByEmployee(scope.currentEmployee()).accessCount(0).build();s=shares.save(s);audit.record("SHARE_LINK_CREATED",MODULE,"VideoShareLink",s.getId(),null,Map.of("videoId",id.toString(),"status","ACTIVE"));return shareResponse(s);}
 @Transactional public void revokeShare(UUID id){VideoShareLink s=shares.findById(id).filter(x->x.getDeletedAt()==null).orElseThrow(()->new ResourceNotFoundException("Share link not found"));if(s.getStatus()!=ShareLinkStatus.ACTIVE)throw new ConflictException("Only active share links can be revoked");s.setStatus(ShareLinkStatus.REVOKED);s.setRevokedAt(Instant.now());shares.save(s);audit.record("SHARE_LINK_REVOKED",MODULE,"VideoShareLink",id,null,Map.of("status","REVOKED"));}
 @Transactional public PublicVideo resolve(String token){VideoShareLink s=shares.findByToken(token).orElseThrow(()->new ResourceNotFoundException("Share link not found"));if(s.getStatus()!=ShareLinkStatus.ACTIVE)throw new BusinessException("Share link is not active","SHARE_LINK_INACTIVE",org.springframework.http.HttpStatus.GONE);if(s.getExpiresAt()!=null&&!s.getExpiresAt().isAfter(Instant.now())){s.setStatus(ShareLinkStatus.EXPIRED);shares.save(s);throw new BusinessException("Share link has expired","SHARE_LINK_EXPIRED",org.springframework.http.HttpStatus.GONE);}MediaVideo v=s.getVideo();if(!Boolean.TRUE.equals(v.getParentVisible())&&v.getStatus()!=MediaVideoStatus.DELIVERED)throw new ForbiddenException("Video is not parent-visible","VIDEO_NOT_PARENT_VISIBLE");s.setAccessCount(s.getAccessCount()+1);s.setLastAccessedAt(Instant.now());shares.save(s);return new PublicVideo(v.getTitle(),v.getDescription(),v.getStudent()==null?null:v.getStudent().getFullName(),v.getVideoType(),storage.presignRead(v.getStorageBucket(),v.getStorageKey()),s.getExpiresAt());}

 @Transactional public ManualZaloResponse prepareZalo(UUID videoId,UUID parentId){MediaVideo v=findVideo(videoId);if(v.getStatus()!=MediaVideoStatus.APPROVED&&v.getStatus()!=MediaVideoStatus.DELIVERED)throw new ConflictException("Video must be approved before delivery","VIDEO_NOT_APPROVED");Parent p=findParent(parentId);validateParent(v,p);List<VideoShareLink> active=shares.findActive(videoId,parentId,Instant.now());VideoShareLink link=active.isEmpty()?createShareEntity(v,p):active.getFirst();String shareUrl=join(media.getPublicShareBaseUrl(),link.getShareToken());String student=v.getStudent()==null?"your child":v.getStudent().getFullName();String message="APIX English Center gửi phụ huynh video của "+student+": "+v.getTitle()+". Xem tại: "+shareUrl;Employee employee=scope.currentEmployee();NotificationDelivery d=NotificationDelivery.builder().entityType("VIDEO").entityId(videoId).parent(p).student(v.getStudent()).channel("MANUAL_ZALO").recipientName(p.getFullName()).recipientPhone(p.getPhone()).messageContent(message).status(DeliveryStatus.PREPARED).preparedBy(employee).build();d=deliveries.save(d);audit.record("MANUAL_ZALO_MESSAGE_PREPARED",MODULE,"NotificationDelivery",d.getId(),null,Map.of("videoId",videoId.toString(),"status","PREPARED"));return new ManualZaloResponse(d.getId(),p.getFullName(),p.getPhone(),"https://zalo.me/"+p.getPhone().replaceAll("\\D",""),message,shareUrl,d.getStatus());}
 @Transactional public DeliveryResponse updateDelivery(UUID id,DeliveryStatusRequest request){NotificationDelivery d=deliveries.findById(id).filter(x->x.getDeletedAt()==null).orElseThrow(()->new ResourceNotFoundException("Delivery not found"));if(request.status()==DeliveryStatus.PREPARED)throw new BusinessException("PREPARED is only set when a delivery is created","INVALID_DELIVERY_TRANSITION");d.setStatus(request.status());d.setNote(request.note());if(request.status()==DeliveryStatus.SENT_MANUALLY){d.setSentBy(scope.currentEmployee());d.setSentAt(Instant.now());MediaVideo v=findVideo(d.getEntityId());v.setStatus(MediaVideoStatus.DELIVERED);v.setDeliveredAt(d.getSentAt());v.setParentVisible(true);videos.save(v);}deliveries.save(d);audit.record(request.status()==DeliveryStatus.SENT_MANUALLY?"MANUAL_ZALO_DELIVERY_MARKED_SENT":"MANUAL_ZALO_DELIVERY_STATUS_UPDATED",MODULE,"NotificationDelivery",id,null,Map.of("status",request.status().name()));return new DeliveryResponse(id,d.getStatus(),d.getSentAt(),d.getNote());}

 private VideoUploadSession activeSession(String token,boolean allowUploading){VideoUploadSession s=sessions.findByToken(token).orElseThrow(()->new ResourceNotFoundException("Upload session not found"));if(!s.getExpiresAt().isAfter(Instant.now())){if(s.getStatus()!=VideoUploadStatus.COMPLETED){s.setStatus(VideoUploadStatus.EXPIRED);sessions.save(s);}throw new BusinessException("Upload session has expired","UPLOAD_SESSION_EXPIRED",org.springframework.http.HttpStatus.GONE);}if(s.getStatus()!=VideoUploadStatus.PENDING&&!(allowUploading&&s.getStatus()==VideoUploadStatus.UPLOADING))throw new ConflictException("Upload session is not available: "+s.getStatus(),"UPLOAD_SESSION_UNAVAILABLE");return s;}
 void validateTarget(VideoType type,UUID classId,UUID studentId,LocalDate month){if(classId==null&&studentId==null)throw new BusinessException("At least one of classId or studentId is required","VIDEO_TARGET_REQUIRED");if(type==VideoType.MONTHLY_PERSONAL_VIDEO&&(studentId==null||month==null))throw new BusinessException("Monthly personal video requires studentId and targetMonth","MONTHLY_VIDEO_TARGET_REQUIRED");if(month!=null&&month.getDayOfMonth()!=1)throw new BusinessException("targetMonth must be the first day of the month","INVALID_TARGET_MONTH");if((type==VideoType.CLASS_ACTIVITY_VIDEO||type==VideoType.FOREIGN_TEACHER_ACTIVITY)&&classId==null)throw new BusinessException("This video type requires classId","CLASS_VIDEO_TARGET_REQUIRED");}
 private void validateFile(String mime,long size){if(media.getVideo().getAllowedMimeTypes().stream().noneMatch(x->x.equalsIgnoreCase(mime)))throw new BusinessException("Unsupported video MIME type","VIDEO_MIME_NOT_ALLOWED");long max=Math.multiplyExact(media.getVideo().getMaxFileSizeMb(),1024L*1024L);if(size<=0||size>max)throw new BusinessException("Video exceeds the configured size limit","VIDEO_FILE_TOO_LARGE");}
 private void enforceCreateScope(Clazz clazz){if(isClassScoped()){if(clazz==null)throw new ForbiddenException("Scoped staff must select an assigned class","VIDEO_CLASS_SCOPE_REQUIRED");scope.requireAssigned(clazz.getId());}}
 private void enforceReadScope(MediaVideo v){if(isClassScoped()){if(v.getClazz()==null)throw new ForbiddenException("Video is outside assigned class scope");scope.requireAssigned(v.getClazz().getId());}}
 private boolean isClassScoped(){return !actor.has("media-video:approve")&&!actor.has("media-video:create-share-link")&&!actor.has("*");}
 private boolean isEnrolled(UUID student,UUID clazz){Integer count=jdbc.queryForObject("select count(*) from class_enrollments where student_id=? and class_id=? and deleted_at is null and status in ('TRIAL','ACTIVE','FROZEN')",Integer.class,student,clazz);return count!=null&&count>0;}
 private MediaVideo findVideo(UUID id){return videos.findDetail(id).orElseThrow(()->new ResourceNotFoundException("Video not found"));}
 private Parent findParent(UUID id){return parents.findById(id).filter(x->x.getDeletedAt()==null).orElseThrow(()->new ResourceNotFoundException("Parent not found"));}
 private void validateParent(MediaVideo v,Parent p){if(v.getStudent()!=null&&studentParents.findByStudentIdAndParentIdAndDeletedAtIsNull(v.getStudent().getId(),p.getId()).isEmpty())throw new ForbiddenException("Parent is not linked to the video's student","PARENT_STUDENT_MISMATCH");}
 private void requireStatus(MediaVideo v,MediaVideoStatus... allowed){if(Arrays.stream(allowed).noneMatch(x->x==v.getStatus()))throw new ConflictException("Video cannot transition from "+v.getStatus());}
 private VideoShareLink createShareEntity(MediaVideo v,Parent p){v.setParentVisible(true);videos.save(v);VideoShareLink s=VideoShareLink.builder().video(v).shareToken(token()).status(ShareLinkStatus.ACTIVE).expiresAt(Instant.now().plus(Duration.ofDays(media.getShareLinkExpiryDays()))).createdForParent(p).createdByEmployee(scope.currentEmployee()).accessCount(0).build();s=shares.save(s);audit.record("SHARE_LINK_CREATED",MODULE,"VideoShareLink",s.getId(),null,Map.of("videoId",v.getId().toString(),"status","ACTIVE"));return s;}
 private ShareLinkResponse shareResponse(VideoShareLink s){return new ShareLinkResponse(s.getId(),join(media.getPublicShareBaseUrl(),s.getShareToken()),s.getShareToken(),s.getExpiresAt(),s.getStatus());}
 private void auditSession(VideoUploadSession s,String action,Map<String,Object> after){audit.recordAs(s.getCreatedBy(),s.getRequestedBy().getId(),action,MODULE,"VideoUploadSession",s.getId(),null,after);}
 private String token(){byte[] bytes=new byte[32];RANDOM.nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
 private String safeFileName(String value){String name=value.replace('\\','/');name=name.substring(name.lastIndexOf('/')+1).replaceAll("[^A-Za-z0-9._-]","_");if(name.isBlank()||name.equals(".")||name.equals(".."))throw new BusinessException("Invalid file name","INVALID_FILE_NAME");return name.length()>180?name.substring(name.length()-180):name;}
 private String join(String base,String token){if(!StringUtils.hasText(base))throw new IllegalStateException("Media frontend base URL is not configured");return base.replaceAll("/+$","")+"/"+token;}
 private String name(Clazz c){return c==null?null:c.getName();} private String name(Student s){return s==null?null:s.getFullName();}
 private String defaultTitle(VideoUploadSession s){String target=s.getStudent()!=null?s.getStudent().getFullName():s.getClazz().getName();return s.getVideoType().name().replace('_',' ')+" - "+target;}
 private CompleteResponse completeResponse(MediaVideo v){return new CompleteResponse(v.getId(),v.getStatus(),v.getTitle(),v.getVideoType(),name(v.getStudent()),name(v.getClazz()),v.getCreatedAt());}
 private VideoListItem listItem(MediaVideo v){return new VideoListItem(v.getId(),v.getTitle(),v.getVideoType(),v.getStudent()==null?null:v.getStudent().getId(),name(v.getStudent()),v.getClazz()==null?null:v.getClazz().getId(),name(v.getClazz()),v.getTargetMonth(),v.getStatus(),v.getOriginalFileName(),v.getFileSizeBytes(),v.getDurationSeconds(),v.getUploadedBy()==null?null:v.getUploadedBy().getFullName(),v.getApprovedBy()==null?null:v.getApprovedBy().getFullName(),v.getApprovedAt(),v.getDeliveredAt(),Boolean.TRUE.equals(v.getParentVisible()),v.getCreatedAt(),v.getUpdatedAt());}
 private VideoDetail detail(MediaVideo v){List<ShareSummary> ss=shares.findByVideoIdAndDeletedAtIsNullOrderByCreatedAtDesc(v.getId()).stream().map(s->new ShareSummary(s.getId(),s.getStatus(),s.getExpiresAt(),s.getAccessCount(),s.getCreatedForParent()==null?null:s.getCreatedForParent().getId())).toList();List<DeliverySummary> ds=deliveries.findByEntityTypeAndEntityIdAndDeletedAtIsNullOrderByCreatedAtDesc("VIDEO",v.getId()).stream().map(d->new DeliverySummary(d.getId(),d.getStatus(),d.getParent()==null?null:d.getParent().getId(),d.getSentAt())).toList();return new VideoDetail(v.getId(),v.getUploadSession()==null?null:v.getUploadSession().getId(),v.getClazz()==null?null:v.getClazz().getId(),name(v.getClazz()),v.getStudent()==null?null:v.getStudent().getId(),name(v.getStudent()),v.getSessionId(),v.getVideoType(),v.getTargetMonth(),v.getTitle(),v.getDescription(),v.getOriginalFileName(),v.getMimeType(),v.getFileSizeBytes(),v.getDurationSeconds(),v.getChecksum(),v.getStatus(),v.getUploadedBy()==null?null:v.getUploadedBy().getFullName(),v.getApprovedBy()==null?null:v.getApprovedBy().getFullName(),v.getApprovedAt(),v.getRejectionReason(),v.getDeliveredAt(),Boolean.TRUE.equals(v.getParentVisible()),storage.presignRead(v.getStorageBucket(),v.getStorageKey()),ss,ds,v.getCreatedAt(),v.getUpdatedAt(),v.getVersion());}
}
