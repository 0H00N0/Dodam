// src/main/java/com/dodam/plan/repository/PlanPaymentRepository.java
package com.dodam.plan.repository;

import com.dodam.plan.Entity.PlanPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanPaymentRepository extends JpaRepository<PlanPaymentEntity, Long> {

    List<PlanPaymentEntity> findAllByMid(String mid);

    Optional<PlanPaymentEntity> findTopByMidOrderByPayIdDesc(String mid);

    Optional<PlanPaymentEntity> findByMidAndPayCustomer(String mid, String payCustomer);

    Optional<PlanPaymentEntity> findByPayCustomer(String payCustomer);
}
