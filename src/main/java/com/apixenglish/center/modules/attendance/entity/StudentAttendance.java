package com.apixenglish.center.modules.attendance.entity;

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

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "student_attendance")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendance extends SoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ClassSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private String status = "NOT_MARKED"; // NOT_MARKED, PRESENT, ABSENT, LATE, EXCUSED

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Column(name = "note")
    private String note;

    @Column(name = "marked_by")
    private UUID markedBy;

    @Column(name = "marked_at")
    private Instant markedAt;

    @Builder.Default
    @Column(name = "source", nullable = false)
    private String source = "SYSTEM"; // TEACHER, OFFICE_STAFF, SYSTEM

    @Builder.Default
    @Column(name = "locked_by_office", nullable = false)
    private Boolean lockedByOffice = false;
}
