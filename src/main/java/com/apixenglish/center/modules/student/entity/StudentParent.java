package com.apixenglish.center.modules.student.entity;

import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.parent.entity.Parent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_parents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentParent extends SoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false)
    private StudentParentRelationship relationship;

    @Column(name = "is_primary_contact")
    private Boolean isPrimaryContact;

    @Column(name = "can_receive_notification")
    private Boolean canReceiveNotification;

    @Column(name = "can_receive_tuition")
    private Boolean canReceiveTuition;

    @Column(name = "can_pickup_student")
    private Boolean canPickupStudent;

    @Column(name = "is_emergency_contact")
    private Boolean isEmergencyContact;
}
