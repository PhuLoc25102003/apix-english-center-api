package com.apixenglish.center.modules.score.entity;
import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import com.apixenglish.center.modules.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;import java.time.*;import java.util.*;
@Entity @Table(name="score_items") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ScoreItem extends SoftDeleteEntity {
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="class_id",nullable=false) private Clazz clazz;
 @Column(nullable=false) private String title;
 @Column(name="max_score",nullable=false) private BigDecimal maxScore;
 @Column(name="score_date",nullable=false) private LocalDate scoreDate;
 private String note;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private ScoreItemStatus status;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by_employee",nullable=false) private Employee createdByEmployee;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="delivered_by") private Employee deliveredBy;
 @Column(name="delivered_at") private Instant deliveredAt;
 @OneToMany(mappedBy="scoreItem",cascade=CascadeType.ALL,orphanRemoval=true) @Builder.Default private List<ScoreRecord> records=new ArrayList<>();
}
