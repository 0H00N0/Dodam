package com.dodam.plan.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.dodam.plan.repository.PlanBenefitRepository;
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
	
	// 활성 플랜 목록 + 1:1 혜택 매칭
	public List<PlanDTO> getActivePlans(){
		List<PlansEntity> plans = pr.findByActiveTrue();
		
		// 혜택을 한꺼번에 가져와서 매핑 (N+1 회피용)
		List<PlanBenefitEntity> benefits = pbr.findByPlanIn(plans);
		Map<Long, PlanBenefitEntity> byPlanId = new HashMap<>();
		for(PlanBenefitEntity b : benefits) {
			byPlanId.put(b.getPlan().getPlanId(), b);
		}
		
		return plans.stream()
				.map(p -> PlanDTO.of(p, byPlanId.get(p.getPlanId())))
				.toList();
	}
	
	public PlanDTO getByCode(String code) {
		PlansEntity plan = pr.findByPlanCode(code)
							.orElseThrow(() -> new IllegalArgumentException("플랜을 찾을 수 없습니다: " + code));
		PlanBenefitEntity benefit = pbr.findFirstByPlan(plan).orElse(null);
		return PlanDTO.of(plan, benefit);
	}
}
