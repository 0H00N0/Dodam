package com.dodam.plan.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.dodam.plan.repository.PlanBenefitRepository;
import com.dodam.plan.repository.PlanPriceRepository;
import com.dodam.plan.repository.PlansRepository;
import com.dodam.plan.Entity.PlanBenefitEntity;
import com.dodam.plan.Entity.PlansEntity;
import com.dodam.plan.dto.PlanDTO;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanService {
	private final PlansRepository pr;
	private final PlanBenefitRepository pbr;
	private final PlanPriceRepository ppr;
	
	// 활성 플랜 목록 + 1:1 혜택 매칭
	public List<PlanDTO> getActivePlans(){
		List<PlansEntity> plans = pr.findByPlanActiveTrue();
		
		// 혜택을 한꺼번에 가져와서 매핑 (N+1 회피용)
		List<PlanBenefitEntity> benefits = pbr.findByPlanIn(plans);
		Map<Long, PlanBenefitEntity> byPlanId = new HashMap<>();
		for(PlanBenefitEntity b : benefits) {
			byPlanId.put(b.getPlan().getPlanId(), b);
		}
		
		return plans.stream()
				.map(p -> {
					PlanBenefitEntity b = byPlanId.get(p.getPlanId());
					var prices = ppr.findByPlan_PlanIdAndPpriceActiveTrue(p.getPlanId());
					return PlanDTO.of(p, b, prices);
				})
				.toList();
	}
	
	public PlanDTO getByCode(String code) {
		PlansEntity plan = pr.findByPlanCode(code)
							.orElseThrow(() -> new IllegalArgumentException("플랜을 찾을 수 없습니다: " + code));
		PlanBenefitEntity benefit = pbr.findFirstByPlan(plan).orElse(null);
		var prices = ppr.findByPlan_PlanIdAndPpriceActiveTrue(plan.getPlanId());
		return PlanDTO.of(plan, benefit, prices);
	}
}
