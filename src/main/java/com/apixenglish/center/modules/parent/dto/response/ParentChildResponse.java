package com.apixenglish.center.modules.parent.dto.response;

import com.apixenglish.center.modules.student.entity.StudentParentRelationship;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentChildResponse {
    private UUID id; // student ID
    private String studentCode;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private StudentParentRelationship relationship;
    private Boolean isPrimaryContact;
    private Boolean canReceiveNotification;
    private Boolean canReceiveTuition;
    private Boolean canPickupStudent;
    private Boolean isEmergencyContact;
    private Instant createdAt;
    private Instant updatedAt;
}
