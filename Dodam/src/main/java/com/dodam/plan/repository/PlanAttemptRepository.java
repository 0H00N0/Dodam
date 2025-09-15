package com.dodam.plan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dodam.plan.Entity.PlanAttemptEntity;

public interface PlanAttemptRepository extends JpaRepository<PlanAttemptEntity, Long> {
	  List<PlanAttemptEntity> findByInvoice_PiIdOrderByPattAtDesc(Long piId);
	}
