package com.apixenglish.center.modules.student.entity;

import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student extends SoftDeleteEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "student_code", nullable = false, unique = true)
    private String studentCode;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "school_name")
    private String schoolName;

    @Column(name = "grade")
    private String grade;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "medical_notes")
    private String medicalNotes;

    @Column(name = "learning_notes")
    private String learningNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_type", nullable = false)
    private StudentType studentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_mode", nullable = false)
    private StudentAccessMode accessMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StudentStatus status;
}
