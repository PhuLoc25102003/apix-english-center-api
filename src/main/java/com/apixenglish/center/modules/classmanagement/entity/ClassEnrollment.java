package com.apixenglish.center.modules.classmanagement.entity;

import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.student.entity.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "class_enrollments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassEnrollment extends SoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Clazz clazz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "enrollment_code", nullable = false, unique = true)
    private String enrollmentCode;

    @Builder.Default
    @Column(name = "enrolled_date", nullable = false)
    private LocalDate enrolledDate = LocalDate.now();

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, TRANSFERRED, FROZEN, COMPLETED, CANCELLED

    @Column(name = "source")
    private String source;

    @Column(name = "note")
    private String note;

    @Column(name = "cancellation_reason")
    private String cancellationReason;
}
