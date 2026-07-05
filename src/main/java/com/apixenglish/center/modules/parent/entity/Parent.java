package com.apixenglish.center.modules.parent.entity;

import com.apixenglish.center.common.domain.SoftDeleteEntity;
import com.apixenglish.center.modules.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Parent extends SoftDeleteEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "parent_code", nullable = false, unique = true)
    private String parentCode;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "note")
    private String note;
}
