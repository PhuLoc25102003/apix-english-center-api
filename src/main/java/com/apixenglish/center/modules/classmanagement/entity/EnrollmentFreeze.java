package com.apixenglish.center.modules.classmanagement.entity;
import com.apixenglish.center.common.domain.SoftDeleteEntity;import jakarta.persistence.*;import lombok.*;import java.time.LocalDate;
@Entity @Table(name="enrollment_freezes") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor public class EnrollmentFreeze extends SoftDeleteEntity{
 @ManyToOne(fetch=FetchType.LAZY)@JoinColumn(name="enrollment_id",nullable=false)private ClassEnrollment enrollment;@Column(name="start_date",nullable=false)private LocalDate startDate;@Column(name="end_date",nullable=false)private LocalDate endDate;@Column(nullable=false)private String reason;@Column(name="previous_status",nullable=false)private String previousStatus;@Column(nullable=false)private String status;
}
