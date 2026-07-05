package com.apixenglish.center.modules.student.dto.response;

import com.apixenglish.center.modules.parent.dto.response.ParentResponse;
import com.apixenglish.center.modules.student.entity.StudentParentRelationship;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentParentResponse {
    private UUID id;
    private UUID studentId;
    private ParentResponse parent;
    private StudentParentRelationship relationship;
    private Boolean isPrimaryContact;
    private Boolean canReceiveNotification;
    private Boolean canReceiveTuition;
    private Boolean canPickupStudent;
    private Boolean isEmergencyContact;
}
