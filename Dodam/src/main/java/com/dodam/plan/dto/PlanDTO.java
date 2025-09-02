package com.dodam.plan.dto;

import com.dodam.plan.Entity.PlansEntity;
import com.dodam.plan.Entity.PlanBenefitEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanDTO {
	private Long planId;
	private String planCode;
	private String planName; //플랜 이름 Basic, Standard ...
	private Boolean planActive;
	private LocalDateTime planCreate;
	
	//혜택
	private BigDecimal pbPriceCap; //월 대여료 상한
	private String pbNote;
	
	public static PlanDTO of(PlansEntity plan, PlanBenefitEntity benefit) {
		return PlanDTO.builder()
				.planId(plan.getPlanId())
				.planCode(plan.getPlanCode())
				.planName(plan.getPlanName().getPlanName())
				.planActive(plan.getPlanActive())
				.planCreate(plan.getPlanCreate())
				.pbPriceCap(benefit != null ? benefit.getPbPriceCap() : null)
				.pbNote(benefit != null ? benefit.getPbNote() : null)
				.build();
	}
}
