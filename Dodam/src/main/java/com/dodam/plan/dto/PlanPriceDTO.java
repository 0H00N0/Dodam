package com.dodam.plan.dto;

import java.math.BigDecimal;

import com.dodam.plan.Entity.PlanPriceEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanPriceDTO {
	private Long ppriceId;
	private Integer termMonth;
	private String bilMode;
	private BigDecimal amount;
	private String currency;
	private Boolean active;
	
	public static PlanPriceDTO of(PlanPriceEntity entity) {
		return PlanPriceDTO.builder()
				.ppriceId(entity.getPpriceId())
				.termMonth(entity.getPterm().getPtermMonth())
				.bilMode(entity.getPpriceBilMode())
				.amount(entity.getPpriceAmount())
				.currency(entity.getPpriceCurr())
				.active(entity.getPpriceActive())
				.build();
	}
}
