package com.apixenglish.center.modules.classmanagement.mapper;

import com.apixenglish.center.modules.classmanagement.dto.response.EnrollmentResponse;
import com.apixenglish.center.modules.classmanagement.entity.ClassEnrollment;
import org.springframework.stereotype.Component;

@Component
public class ClassEnrollmentMapper {

    public EnrollmentResponse toResponse(ClassEnrollment enrollment) {
        if (enrollment == null) {
            return null;
        }

        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .classId(enrollment.getClazz() != null ? enrollment.getClazz().getId() : null)
                .className(enrollment.getClazz() != null ? enrollment.getClazz().getName() : null)
                .classCode(enrollment.getClazz() != null ? enrollment.getClazz().getClassCode() : null)
                .studentId(enrollment.getStudent() != null ? enrollment.getStudent().getId() : null)
                .studentName(enrollment.getStudent() != null ? enrollment.getStudent().getFullName() : null)
                .studentCode(enrollment.getStudent() != null ? enrollment.getStudent().getStudentCode() : null)
                .enrollmentCode(enrollment.getEnrollmentCode())
                .enrolledDate(enrollment.getEnrolledDate())
                .startDate(enrollment.getStartDate())
                .endDate(enrollment.getEndDate())
                .status(enrollment.getStatus())
                .source(enrollment.getSource())
                .note(enrollment.getNote())
                .build();
    }
}
