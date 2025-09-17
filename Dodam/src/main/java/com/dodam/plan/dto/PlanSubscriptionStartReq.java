package com.dodam.plan.dto;

import lombok.Data;

@Data
public class PlanSubscriptionStartReq {
	private String planCode; //같은 플랜 중복방지
	private Integer months;
}
