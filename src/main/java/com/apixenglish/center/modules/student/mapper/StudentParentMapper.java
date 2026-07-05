package com.apixenglish.center.modules.student.mapper;

import com.apixenglish.center.modules.parent.dto.response.ParentChildResponse;
import com.apixenglish.center.modules.student.dto.response.StudentParentResponse;
import com.apixenglish.center.modules.student.entity.StudentParent;
import org.springframework.stereotype.Component;

@Component
public class StudentParentMapper {

    public StudentParentResponse toResponse(StudentParent studentParent) {
        if (studentParent == null) {
            return null;
        }

        return StudentParentResponse.builder()
                .id(studentParent.getParent().getId())
                .parentCode(studentParent.getParent().getParentCode())
                .fullName(studentParent.getParent().getFullName())
                .phone(studentParent.getParent().getPhone())
                .email(studentParent.getParent().getEmail())
                .relationship(studentParent.getRelationship())
                .isPrimaryContact(studentParent.getIsPrimaryContact())
                .canReceiveNotification(studentParent.getCanReceiveNotification())
                .canReceiveTuition(studentParent.getCanReceiveTuition())
                .canPickupStudent(studentParent.getCanPickupStudent())
                .isEmergencyContact(studentParent.getIsEmergencyContact())
                .createdAt(studentParent.getCreatedAt())
                .updatedAt(studentParent.getUpdatedAt())
                .build();
    }

    public ParentChildResponse toParentChildResponse(StudentParent studentParent) {
        if (studentParent == null) {
            return null;
        }

        return ParentChildResponse.builder()
                .id(studentParent.getStudent().getId())
                .studentCode(studentParent.getStudent().getStudentCode())
                .fullName(studentParent.getStudent().getFullName())
                .dateOfBirth(studentParent.getStudent().getDateOfBirth())
                .gender(studentParent.getStudent().getGender())
                .relationship(studentParent.getRelationship())
                .isPrimaryContact(studentParent.getIsPrimaryContact())
                .canReceiveNotification(studentParent.getCanReceiveNotification())
                .canReceiveTuition(studentParent.getCanReceiveTuition())
                .canPickupStudent(studentParent.getCanPickupStudent())
                .isEmergencyContact(studentParent.getIsEmergencyContact())
                .createdAt(studentParent.getCreatedAt())
                .updatedAt(studentParent.getUpdatedAt())
                .build();
    }
}
