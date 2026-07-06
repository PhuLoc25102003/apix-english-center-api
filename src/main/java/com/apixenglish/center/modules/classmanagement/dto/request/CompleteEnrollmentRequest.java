package com.apixenglish.center.modules.classmanagement.dto.request;import jakarta.validation.constraints.NotNull;import lombok.*;import java.time.LocalDate;
@Data @NoArgsConstructor @AllArgsConstructor public class CompleteEnrollmentRequest{@NotNull private LocalDate completedDate;private String note;}
