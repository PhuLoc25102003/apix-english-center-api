package com.apixenglish.center.modules.classmanagement.dto.request;import jakarta.validation.constraints.*;import lombok.*;import java.time.LocalDate;
@Data @NoArgsConstructor @AllArgsConstructor public class FreezeEnrollmentRequest{@NotNull private LocalDate startDate;@NotNull private LocalDate endDate;@NotBlank private String reason;}
