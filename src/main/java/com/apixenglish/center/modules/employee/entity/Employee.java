package com.apixenglish.center.modules.employee.entity;

import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employees")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Employee extends SoftDeleteEntity {
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    @Column(name = "employee_code", nullable = false, unique = true) private String employeeCode;
    @Column(name = "full_name", nullable = false) private String fullName;
    @Column(name = "employment_status", nullable = false) private String employmentStatus;
}
