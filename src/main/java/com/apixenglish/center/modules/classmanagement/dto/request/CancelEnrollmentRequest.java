package com.apixenglish.center.modules.classmanagement.dto.request;import jakarta.validation.constraints.NotBlank;import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor public class CancelEnrollmentRequest{@NotBlank private String reason;}
