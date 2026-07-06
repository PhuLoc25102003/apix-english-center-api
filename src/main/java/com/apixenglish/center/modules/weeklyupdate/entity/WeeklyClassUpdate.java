package com.apixenglish.center.modules.weeklyupdate.entity;

import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import com.apixenglish.center.modules.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="weekly_class_updates")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class WeeklyClassUpdate extends SoftDeleteEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="class_id",nullable=false) private Clazz clazz;
    @Column(name="week_start_date",nullable=false) private LocalDate weekStartDate;
    @Column(name="week_end_date",nullable=false) private LocalDate weekEndDate;
    @Column(name="homework_text") private String homeworkText;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private WeeklyUpdateStatus status;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="prepared_by",nullable=false) private Employee preparedBy;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="approved_by") private Employee approvedBy;
    @Column(name="approved_at") private Instant approvedAt;
    @Column(name="rejection_reason") private String rejectionReason;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="delivered_by") private Employee deliveredBy;
    @Column(name="delivered_at") private Instant deliveredAt;
    @OneToMany(mappedBy="weeklyUpdate",cascade=CascadeType.ALL,orphanRemoval=true)
    @OrderBy("displayOrder asc") @Builder.Default private List<WeeklyUpdateSession> sessions = new ArrayList<>();
}
