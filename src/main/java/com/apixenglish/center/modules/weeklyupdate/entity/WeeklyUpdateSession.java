package com.apixenglish.center.modules.weeklyupdate.entity;
import com.apixenglish.center.common.domain.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
@Entity @Table(name="weekly_update_sessions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class WeeklyUpdateSession extends SoftDeleteEntity {
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="weekly_update_id",nullable=false) private WeeklyClassUpdate weeklyUpdate;
 @Column(name="session_date",nullable=false) private LocalDate sessionDate;
 private String title;
 @Column(nullable=false) private String content;
 @Column(name="display_order",nullable=false) private Integer displayOrder;
}
