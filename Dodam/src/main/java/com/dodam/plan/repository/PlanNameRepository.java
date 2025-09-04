package com.dodam.plan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dodam.plan.Entity.PlanNameEntity;

public interface PlanNameRepository extends JpaRepository<PlanNameEntity, Long>{
	Optional<PlanNameEntity> findByPlanName(String planName);
}
