package com.apixenglish.center.modules.student.dto.response;

import com.apixenglish.center.modules.student.entity.StudentAccessMode;
import com.apixenglish.center.modules.student.entity.StudentStatus;
import com.apixenglish.center.modules.student.entity.StudentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private UUID id;
    private UUID userId;
    private String studentCode;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String schoolName;
    private String grade;
    private String avatarUrl;
    private String medicalNotes;
    private String learningNotes;
    private StudentType studentType;
    private StudentAccessMode accessMode;
    private StudentStatus status;
}
