package com.dodam.plan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dodam.plan.Entity.PlanBenefitEntity;
import com.dodam.plan.Entity.PlansEntity;

public interface PlanBenefitRepository extends JpaRepository<PlanBenefitEntity, Long>{
	Optional<PlanBenefitEntity> findFirstByPlan(PlansEntity plan);
	List<PlanBenefitEntity> findByPlanIn(List<PlansEntity> plans);
	// =================== ▼ 추가된 부분 시작 ▼ ===================
		/**
		 * 특정 플랜에 속한 모든 혜택 정보를 조회
		 */
		List<PlanBenefitEntity> findByPlan_PlanId(Long planId);

		/**
		 * 특정 플랜에 속한 모든 혜택 정보를 삭제
		 */
		void deleteByPlan_PlanId(Long planId);
		// =================== ▲ 추가된 부분 끝 ▲ ===================
}
