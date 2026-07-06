package com.apixenglish.center.modules.classmanagement.dto.response;
import lombok.*;import java.math.BigDecimal;import java.time.*;import java.util.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor public class EnrollmentDetailResponse{
 private EnrollmentResponse enrollment;private StudentSummary student;private ClassSummary clazz;
 @Builder.Default private List<ParentSummary> parents=List.of();@Builder.Default private List<TransferSummary> transferHistory=List.of();@Builder.Default private List<FreezeSummary> freezeHistory=List.of();private TuitionSummary tuitionSummary;@Builder.Default private List<AuditSummary> auditSummary=List.of();
 @Data @Builder @NoArgsConstructor @AllArgsConstructor public static class StudentSummary{private UUID id;private String studentCode;private String fullName;private LocalDate dateOfBirth;private String status;}
 @Data @Builder @NoArgsConstructor @AllArgsConstructor public static class ClassSummary{private UUID id;private String classCode;private String name;private UUID courseId;private String courseName;private String levelName;private String campusName;}
 @Data @Builder @NoArgsConstructor @AllArgsConstructor public static class ParentSummary{private UUID id;private String parentCode;private String fullName;private String relationship;private Boolean primaryContact;private String phone;private String email;}
 @Data @Builder @NoArgsConstructor @AllArgsConstructor public static class TransferSummary{private UUID id;private UUID fromEnrollmentId;private UUID toEnrollmentId;private UUID fromClassId;private UUID toClassId;private LocalDate transferDate;private String reason;}
 @Data @Builder @NoArgsConstructor @AllArgsConstructor public static class FreezeSummary{private UUID id;private LocalDate startDate;private LocalDate endDate;private String reason;private String status;}
 @Data @Builder @NoArgsConstructor @AllArgsConstructor public static class TuitionSummary{private BigDecimal totalAmount;private BigDecimal paidAmount;private BigDecimal remainingAmount;private long invoiceCount;}
 @Data @Builder @NoArgsConstructor @AllArgsConstructor public static class AuditSummary{private String action;private Instant createdAt;private UUID actorUserId;}
}
