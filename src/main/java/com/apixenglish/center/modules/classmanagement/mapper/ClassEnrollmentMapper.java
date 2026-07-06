package com.apixenglish.center.modules.classmanagement.mapper;
import com.apixenglish.center.modules.classmanagement.dto.response.EnrollmentResponse;import com.apixenglish.center.modules.classmanagement.entity.ClassEnrollment;import org.springframework.stereotype.Component;
@Component public class ClassEnrollmentMapper{
 public EnrollmentResponse toResponse(ClassEnrollment e){if(e==null)return null;var c=e.getClazz();var course=c==null?null:c.getCourse();var level=course==null?null:course.getLevel();var campus=c==null?null:c.getCampus();return EnrollmentResponse.builder()
  .id(e.getId()).enrollmentCode(e.getEnrollmentCode()).studentId(e.getStudent().getId()).studentCode(e.getStudent().getStudentCode()).studentFullName(e.getStudent().getFullName())
  .classId(c.getId()).classCode(c.getClassCode()).className(c.getName()).courseId(course==null?null:course.getId()).courseName(course==null?null:course.getName()).levelId(level==null?null:level.getId()).levelName(level==null?null:level.getName()).campusId(campus==null?null:campus.getId()).campusName(campus==null?null:campus.getName())
  .enrolledDate(e.getEnrolledDate()).startDate(e.getStartDate()).endDate(e.getEndDate()).status(e.getStatus()).source(e.getSource()).note(e.getNote()).cancellationReason(e.getCancellationReason()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).version(e.getVersion()).build();}
}
