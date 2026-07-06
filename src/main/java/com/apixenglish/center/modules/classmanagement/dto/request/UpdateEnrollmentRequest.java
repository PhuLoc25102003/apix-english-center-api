package com.apixenglish.center.modules.classmanagement.dto.request;
import jakarta.validation.constraints.*;import lombok.*;import java.time.LocalDate;
@Data @Builder @NoArgsConstructor @AllArgsConstructor public class UpdateEnrollmentRequest{
 @NotNull private LocalDate enrolledDate;@NotNull private LocalDate startDate;private LocalDate endDate;
 @NotBlank @Pattern(regexp="TRIAL|ACTIVE|FROZEN|TRANSFERRED|COMPLETED|CANCELLED") private String status;
 @NotBlank @Pattern(regexp="WALK_IN|REFERRAL|ONLINE|OTHER") private String source;private String note;
}
