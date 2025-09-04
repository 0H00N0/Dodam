package com.dodam.plan.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanDetailDTO {
	private Long planId;
	private String planCode;
	private String displayName;
	private Long monthlyPrice;
	private Long rentalPriceCap;
	private String currency;
	
	private String note; 
	private List<String> Benefits;
}
