package com.apixenglish.center.modules.weeklyupdate.service;

import com.apixenglish.center.common.exception.*;
import com.apixenglish.center.modules.audit.service.AuditService;
import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import com.apixenglish.center.modules.classmanagement.repository.ClazzRepository;
import com.apixenglish.center.modules.classmanagement.service.ClassScopeGuard;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.weeklyupdate.dto.WeeklyUpdateDtos.*;
import com.apixenglish.center.modules.weeklyupdate.entity.*;
import com.apixenglish.center.modules.weeklyupdate.repository.WeeklyClassUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service @RequiredArgsConstructor
public class WeeklyUpdateService {
 private final WeeklyClassUpdateRepository repository; private final ClazzRepository clazzRepository;
 private final ClassScopeGuard scope; private final AuditService audit;
 @Transactional public Response create(Upsert request) {
  if(request.weekEndDate().isBefore(request.weekStartDate())) throw new BusinessException("Week end date must be on or after week start date","INVALID_WEEK_RANGE");
  Employee employee=scope.requireAssigned(request.classId());
  Clazz clazz=clazzRepository.findById(request.classId()).filter(c->c.getDeletedAt()==null).orElseThrow(()->new ResourceNotFoundException("Class not found"));
  WeeklyClassUpdate update=WeeklyClassUpdate.builder().clazz(clazz).weekStartDate(request.weekStartDate()).weekEndDate(request.weekEndDate()).homeworkText(request.homeworkText()).status(WeeklyUpdateStatus.DRAFT).preparedBy(employee).build();
  replaceSessions(update,request.sessions()); update=repository.save(update); audit.record("WEEKLY_UPDATE_CREATED","weekly_update","WeeklyClassUpdate",update.getId(),null,Map.of("status",update.getStatus().name())); return map(update);
 }
 @Transactional(readOnly=true) public Page<Response> list(UUID classId,Pageable pageable){ return repository.findByClazzIdAndDeletedAtIsNull(classId,pageable).map(this::map); }
 @Transactional(readOnly=true) public Response get(UUID id){return map(find(id));}
 @Transactional public Response update(UUID id,Upsert request){ WeeklyClassUpdate u=find(id); scope.requireAssigned(u.getClazz().getId()); require(u,WeeklyUpdateStatus.DRAFT,WeeklyUpdateStatus.REJECTED); u.setWeekStartDate(request.weekStartDate());u.setWeekEndDate(request.weekEndDate());u.setHomeworkText(request.homeworkText());u.setStatus(WeeklyUpdateStatus.DRAFT);u.setRejectionReason(null);replaceSessions(u,request.sessions());audit.record("WEEKLY_UPDATE_UPDATED","weekly_update","WeeklyClassUpdate",id,null,Map.of("status","DRAFT"));return map(repository.save(u)); }
 @Transactional public Response submit(UUID id){WeeklyClassUpdate u=find(id);scope.requireAssigned(u.getClazz().getId());require(u,WeeklyUpdateStatus.DRAFT);u.setStatus(WeeklyUpdateStatus.SUBMITTED);return saveAction(u,"WEEKLY_UPDATE_SUBMITTED");}
 @Transactional public Response approve(UUID id){WeeklyClassUpdate u=find(id);require(u,WeeklyUpdateStatus.SUBMITTED);u.setStatus(WeeklyUpdateStatus.APPROVED);u.setApprovedBy(scope.currentEmployee());u.setApprovedAt(Instant.now());return saveAction(u,"WEEKLY_UPDATE_APPROVED");}
 @Transactional public Response reject(UUID id,String reason){WeeklyClassUpdate u=find(id);require(u,WeeklyUpdateStatus.SUBMITTED);u.setStatus(WeeklyUpdateStatus.REJECTED);u.setRejectionReason(reason);u.setApprovedBy(scope.currentEmployee());u.setApprovedAt(Instant.now());return saveAction(u,"WEEKLY_UPDATE_REJECTED");}
 @Transactional public Response deliver(UUID id){WeeklyClassUpdate u=find(id);require(u,WeeklyUpdateStatus.APPROVED);u.setStatus(WeeklyUpdateStatus.DELIVERED);u.setDeliveredBy(scope.currentEmployee());u.setDeliveredAt(Instant.now());return saveAction(u,"WEEKLY_UPDATE_DELIVERED");}
 private Response saveAction(WeeklyClassUpdate u,String action){repository.save(u);audit.record(action,"weekly_update","WeeklyClassUpdate",u.getId(),null,Map.of("status",u.getStatus().name()));return map(u);}
 private void replaceSessions(WeeklyClassUpdate u,List<Session> values){u.getSessions().clear();for(int i=0;i<values.size();i++){Session s=values.get(i);if(s.sessionDate().isBefore(u.getWeekStartDate())||s.sessionDate().isAfter(u.getWeekEndDate()))throw new BusinessException("Session date must be inside the update week","SESSION_OUTSIDE_WEEK");u.getSessions().add(WeeklyUpdateSession.builder().weeklyUpdate(u).sessionDate(s.sessionDate()).title(s.title()).content(s.content()).displayOrder(i).build());}}
 private WeeklyClassUpdate find(UUID id){return repository.findById(id).filter(u->u.getDeletedAt()==null).orElseThrow(()->new ResourceNotFoundException("Weekly update not found"));}
 private void require(WeeklyClassUpdate u,WeeklyUpdateStatus... allowed){if(Arrays.stream(allowed).noneMatch(s->s==u.getStatus()))throw new ConflictException("Weekly update cannot transition from "+u.getStatus());}
 private Response map(WeeklyClassUpdate u){return new Response(u.getId(),u.getClazz().getId(),u.getWeekStartDate(),u.getWeekEndDate(),u.getHomeworkText(),u.getStatus(),u.getPreparedBy().getId(),u.getApprovedBy()==null?null:u.getApprovedBy().getId(),u.getApprovedAt(),u.getRejectionReason(),u.getDeliveredAt(),u.getSessions().stream().map(s->new Session(s.getSessionDate(),s.getTitle(),s.getContent())).toList(),u.getVersion());}
}
