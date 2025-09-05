package com.dodam.plan.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="PlanBenefit")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanBenefitEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long pbId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="planId", nullable=false, foreignKey=@ForeignKey(name="fk_benefit_plan"))
  private PlansEntity plan;

  // (필요 시 여기에 혜택 상세 필드 추가)
}
