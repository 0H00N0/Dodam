package com.dodam.plan.dto;

import com.dodam.plan.Entity.PlansEntity;
import com.dodam.plan.Entity.PlanBenefitEntity;
import com.dodam.plan.Entity.PlanPriceEntity;


import java.util.List;
import java.util.stream.Collectors;

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
	private Boolean active;
	
	// 혜택
    private String benefitNote;
    private String benefitCap; // 금액 문자열로 내려도 되고 BigDecimal 로 내려도 됨
	
	private List<PlanPriceDTO> prices;
	
	public static PlanDTO of(PlansEntity plan, PlanBenefitEntity benefit, List<PlanPriceEntity> prices) {
		return PlanDTO.builder()
				.planId(plan.getPlanId())
				.planCode(plan.getPlanCode())
				.planName(plan.getPlanName().getPlanName())
				.active(plan.getPlanActive())
				.benefitNote(benefit != null ? benefit.getPbNote() : null)
				.benefitCap(benefit != null ? benefit.getPbPriceCap().toPlainString() : null)
				.prices(prices != null ? prices.stream().map(PlanPriceDTO::of).collect(Collectors.toList()) : null)
				.build();
				
	}
}
