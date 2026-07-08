package com.apixenglish.center.modules.student.dto.request;

import com.apixenglish.center.modules.student.entity.StudentStatus;
import com.apixenglish.center.modules.student.entity.StudentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentRequest {

    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    @NotNull(message = "Date of birth must not be null")
    private LocalDate dateOfBirth;

    private String gender;
    private String schoolName;
    private String grade;
    private String avatarUrl;
    private String medicalNotes;
    private String learningNotes;

    @NotNull(message = "Student type must not be null")
    private StudentType studentType;

    @NotNull(message = "Status must not be null")
    private StudentStatus status;
}
