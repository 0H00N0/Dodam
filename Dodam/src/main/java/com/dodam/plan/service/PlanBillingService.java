package com.dodam.plan.service;

import org.springframework.stereotype.Service;
import com.dodam.plan.Entity.*;
import com.dodam.plan.enums.*;
import com.dodam.plan.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class PlanBillingService {
  private final PlanInvoiceRepo invoiceRepo;
  private final PlanAttemptRepo attemptRepo;

  @Transactional
  public void recordAttempt(Long piId, boolean success, String failReason, String respUid, String receiptUrl, String respJson) {
    var inv = invoiceRepo.findById(piId).orElseThrow();

    var att = PlanAttemptEntity.builder()
      .invoice(inv)
      .pattResult(success ? PattResult.SUCCESS : PattResult.FAIL)
      .pattFail(success ? null : failReason)
      .pattUid(respUid)
      .pattUrl(receiptUrl)
      .pattResponse(respJson)
      .build();
    attemptRepo.save(att);

    inv.setPiStat(success ? PiStatus.PAID : PiStatus.FAILED);
    if (success) inv.setPiPaid(java.time.LocalDateTime.now());
    invoiceRepo.save(inv);
  }
}
