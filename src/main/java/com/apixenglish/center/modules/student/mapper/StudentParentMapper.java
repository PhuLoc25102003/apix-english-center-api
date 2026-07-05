package com.apixenglish.center.modules.student.mapper;

import com.apixenglish.center.modules.parent.mapper.ParentMapper;
import com.apixenglish.center.modules.student.dto.response.StudentParentResponse;
import com.apixenglish.center.modules.student.entity.StudentParent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentParentMapper {

    private final ParentMapper parentMapper;

    public StudentParentResponse toResponse(StudentParent studentParent) {
        if (studentParent == null) {
            return null;
        }

        return StudentParentResponse.builder()
                .id(studentParent.getId())
                .studentId(studentParent.getStudent().getId())
                .parent(parentMapper.toResponse(studentParent.getParent()))
                .relationship(studentParent.getRelationship())
                .isPrimaryContact(studentParent.getIsPrimaryContact())
                .canReceiveNotification(studentParent.getCanReceiveNotification())
                .canReceiveTuition(studentParent.getCanReceiveTuition())
                .canPickupStudent(studentParent.getCanPickupStudent())
                .isEmergencyContact(studentParent.getIsEmergencyContact())
                .build();
    }
}
