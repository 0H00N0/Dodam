package com.dodam.plan.Entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.ForeignKey;

@Entity
@Table(
    name = "Plans",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_plans_planCode", columnNames = "planCode")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlansEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;            // ✅ Long 권장

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "planNameId",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_plan_planName") // ✅ import jakarta.persistence.ForeignKey;
    )
    private PlanNameEntity planName;

    @Column(nullable = false, length = 30, unique = true)
    private String planCode;

    @Column(nullable = false)
    private Boolean planActive;

    @CreationTimestamp                 // ✅ 생성 시 자동 입력
    @Column(nullable = false)
    private LocalDateTime planCreate;
}