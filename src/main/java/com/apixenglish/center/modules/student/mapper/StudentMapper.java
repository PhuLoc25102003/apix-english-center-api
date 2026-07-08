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
                .status(student.getStatus())
                .build();
    }
}
