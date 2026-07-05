package com.apixenglish.center.modules.student.mapper;

import com.apixenglish.center.modules.student.dto.response.StudentResponse;
import com.apixenglish.center.modules.student.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public StudentResponse toResponse(Student student) {
        if (student == null) {
            return null;
        }

        return StudentResponse.builder()
                .id(student.getId())
                .userId(student.getUser() != null ? student.getUser().getId() : null)
                .studentCode(student.getStudentCode())
                .fullName(student.getFullName())
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender())
                .schoolName(student.getSchoolName())
                .grade(student.getGrade())
                .avatarUrl(student.getAvatarUrl())
                .medicalNotes(student.getMedicalNotes())
                .learningNotes(student.getLearningNotes())
                .studentType(student.getStudentType())
                .accessMode(student.getAccessMode())
                .status(student.getStatus())
                .build();
    }
}
