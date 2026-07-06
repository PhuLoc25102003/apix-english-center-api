package com.apixenglish.center.modules.score.entity;
import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.employee.entity.Employee;
import com.apixenglish.center.modules.student.entity.Student;
import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;import java.time.Instant;
@Entity @Table(name="score_records") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ScoreRecord extends SoftDeleteEntity {
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="score_item_id",nullable=false) private ScoreItem scoreItem;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="student_id",nullable=false) private Student student;
 @Column(nullable=false) private BigDecimal score;
 private String comment;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="graded_by",nullable=false) private Employee gradedBy;
 @Column(name="graded_at",nullable=false) private Instant gradedAt;
}
