package com.dodam.plan.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dodam.plan.Entity.*;
import com.dodam.plan.enums.*;

public interface PlanPaymentRepo extends JpaRepository<PlanPaymentEntity, Long> {
  boolean existsByMember_MnumAndPayCustomer(Long mnum, String payCustomer);
  PlanPaymentEntity findTopByMember_MnumAndPayCustomer(Long mnum, String payCustomer);
}

public interface PlanMemberRepo extends JpaRepository<PlanMember, Long> {
  List<PlanMember> findByMember_MnumAndPmStat(Long mnum, PmStatus pmStat);
  List<PlanMember> findByPmNextBilBeforeAndPmStat(LocalDateTime now, PmStatus pmStat);
}

public interface PlanInvoiceRepo extends JpaRepository<PlanInvoiceEntity, Long> {
  PlanInvoiceEntity findByPiUid(String piUid);
  List<PlanInvoiceEntity> findByPlanMember_PmIdOrderByPiStartDesc(Long pmId);
}

public interface PlanAttemptRepo extends JpaRepository<PlanAttemptEntity, Long> {
  List<PlanAttemptEntity> findByInvoice_PiIdOrderByPattAtDesc(Long piId);
}

public interface PlanRefundRepo extends JpaRepository<PlanRefundEntity, Long> {
  List<PlanRefundEntity> findByInvoice_PiId(Long piId);
}
