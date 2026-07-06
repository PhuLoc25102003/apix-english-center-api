package com.apixenglish.center.modules.classmanagement.dto.response;
import lombok.*;import java.time.*;import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor public class EnrollmentResponse{
 private UUID id;private String enrollmentCode;
 private UUID studentId;private String studentCode;private String studentFullName;
 private UUID classId;private String classCode;private String className;
 private UUID courseId;private String courseName;private UUID levelId;private String levelName;
 private UUID campusId;private String campusName;
 private LocalDate enrolledDate;private LocalDate startDate;private LocalDate endDate;
 private String status;private String source;private String note;private String cancellationReason;
 private Instant createdAt;private Instant updatedAt;private Integer version;
}
