package com.apixenglish.center.modules.classmanagement.dto.request;import jakarta.validation.constraints.*;import lombok.*;import java.time.LocalDate;import java.util.UUID;
@Data @NoArgsConstructor @AllArgsConstructor public class TransferEnrollmentRequest{@NotNull private UUID toClassId;@NotNull private LocalDate transferDate;@NotBlank private String reason;}
