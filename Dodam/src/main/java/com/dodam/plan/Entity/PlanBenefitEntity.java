package com.dodam.plan.Entity;

import java.math.BigDecimal;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.ForeignKey;

@Entity
@Table(name = "PlanBenefit")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanBenefitEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pbId;   // PK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planId", nullable = false,
            foreignKey = @ForeignKey(name = "fk_planBenefit_plan"))
    @OnDelete(action = OnDeleteAction.CASCADE) // Plan 삭제 시 혜택도 삭제
    private PlansEntity plan;   // FK → Plan.planId

    @Column(precision = 11, scale = 2)
    private BigDecimal pbPriceCap; // 월 대여료 상한

    @Lob
    @Column(nullable = false)
    private String pbNote; // 혜택 설명  
}
