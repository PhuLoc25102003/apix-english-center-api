package com.apixenglish.center.modules.student.dto.request;

import com.apixenglish.center.modules.student.entity.StudentParentRelationship;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkStudentParentRequest {

    @NotNull(message = "Parent ID must not be null")
    private UUID parentId;

    @NotNull(message = "Relationship must not be null")
    private StudentParentRelationship relationship;

    private Boolean isPrimaryContact;
    private Boolean canReceiveNotification;
    private Boolean canReceiveTuition;
    private Boolean canPickupStudent;
    private Boolean isEmergencyContact;
}
