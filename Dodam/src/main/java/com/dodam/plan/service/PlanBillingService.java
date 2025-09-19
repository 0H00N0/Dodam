// src/main/java/com/dodam/plan/service/PlanBillingService.java
package com.dodam.plan.service;

import com.dodam.plan.Entity.PlanAttemptEntity;
import com.dodam.plan.Entity.PlanInvoiceEntity;
import com.dodam.plan.enums.PlanEnums.PattResult;
import com.dodam.plan.enums.PlanEnums.PiStatus;
import com.dodam.plan.repository.PlanAttemptRepository;
import com.dodam.plan.repository.PlanInvoiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlanBillingService {
  private final PlanInvoiceRepository invoiceRepo;
  private final PlanAttemptRepository attemptRepo;

  @Transactional
  public void recordAttempt(Long piId, boolean success, String failReason, String respUid, String receiptUrl, String respJson) {
    PlanInvoiceEntity inv = invoiceRepo.findById(piId).orElseThrow();

    PlanAttemptEntity att = PlanAttemptEntity.builder()
      .invoice(inv)
      .pattResult(success ? PattResult.SUCCESS : PattResult.FAIL)
      .pattFail(success ? null : failReason)
      .pattUid(respUid)
      .pattUrl(receiptUrl)
      .pattResponse(respJson)
      .build();
    attemptRepo.save(att);

    inv.setPiStat(success ? PiStatus.PAID : PiStatus.FAILED);
    if (success) inv.setPiPaid(LocalDateTime.now());   // ← piPaid 사용
    invoiceRepo.save(inv);
  }
}
