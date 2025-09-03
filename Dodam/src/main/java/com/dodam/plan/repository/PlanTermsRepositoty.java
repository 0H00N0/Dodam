package com.dodam.plan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dodam.plan.Entity.PlanTermsEntity;

public interface PlanTermsRepositoty extends JpaRepository<PlanTermsEntity, Long>{
	Optional<PlanTermsEntity> findByPtermMonth(Integer ptermMonth);
}
