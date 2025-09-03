package com.dodam.plan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dodam.plan.Entity.PlanPriceEntity;

public interface PlanPriceRepository extends JpaRepository<PlanPriceEntity, Long>{
	List<PlanPriceEntity> findByPlan_PlanIdAndPpriceActiveTrue(Long planId);
	Optional<PlanPriceEntity> findByPlan_PlanIdAndPterm_PtermIdAndPpriceBilModeAndPpriceActiveTrue(
		    Long planId, Long ptermId, String bilMode);
}
