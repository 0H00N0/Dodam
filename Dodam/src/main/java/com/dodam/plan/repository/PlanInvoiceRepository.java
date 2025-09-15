package com.dodam.plan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dodam.plan.Entity.PlanInvoiceEntity;

public interface PlanInvoiceRepository extends JpaRepository<PlanInvoiceEntity, Long> {
	Optional<PlanInvoiceEntity> findByPiUid(String piUid);
	List<PlanInvoiceEntity> findByPlanMember_PmIdOrderByPiStartDesc(Long pmId);
}