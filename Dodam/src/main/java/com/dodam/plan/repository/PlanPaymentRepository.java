package com.dodam.plan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dodam.plan.Entity.PlanPaymentEntity;

public interface PlanPaymentRepository extends JpaRepository<PlanPaymentEntity, Long> {
	  boolean existsByMember_MnumAndPayCustomer(Long mnum, String payCustomer);
	  PlanPaymentEntity findTopByMember_MnumAndPayCustomer(Long mnum, String payCustomer);
	  List<PlanPaymentEntity> findByMember_Mnum(Long mnum);

	  Optional<PlanPaymentEntity> findByPayIdAndMember_Mnum(Long payId, Long mnum);
}
