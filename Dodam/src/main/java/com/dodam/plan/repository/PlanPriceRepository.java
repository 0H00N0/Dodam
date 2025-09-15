package com.dodam.plan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dodam.plan.Entity.PlanPriceEntity;

public interface PlanPriceRepository extends JpaRepository<PlanPriceEntity, Long>{
	List<PlanPriceEntity> findByPlan_PlanIdAndPpriceActiveTrue(Long planId);
	Optional<PlanPriceEntity> findByPlan_PlanIdAndPterm_PtermIdAndPpriceBilModeAndPpriceActiveTrue(
		    Long planId, Long ptermId, String bilMode);
	// =================== ▼ 추가된 부분 시작 ▼ ===================
		/**
		 * 특정 플랜에 속한 모든 가격 정보를 조회 (활성화 여부 무관)
		 */
		List<PlanPriceEntity> findByPlan_PlanId(Long planId);

		/**
		 * 특정 플랜에 속한 모든 가격 정보를 삭제
		 */
		void deleteByPlan_PlanId(Long planId);
		// =================== ▲ 추가된 부분 끝 ▲ ===================
}
