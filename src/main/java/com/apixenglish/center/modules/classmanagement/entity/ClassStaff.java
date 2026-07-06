package com.apixenglish.center.modules.classmanagement.entity;

import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "class_staff")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ClassStaff extends SoftDeleteEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "class_id", nullable = false) private Clazz clazz;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "employee_id", nullable = false) private Employee employee;
    @Column(name = "staff_role", nullable = false) private String staffRole;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date") private LocalDate endDate;
    @Column(name = "is_primary", nullable = false) private Boolean isPrimary;
}
