// src/main/java/com/dodam/plan/service/PlanBillingService.java
package com.dodam.plan.service;

import org.springframework.stereotype.Service;
import com.dodam.plan.Entity.*;
import com.dodam.plan.enums.PlanEnums.PattResult;
import com.dodam.plan.enums.PlanEnums.PiStatus;
import com.dodam.plan.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlanBillingService {
  private final PlanInvoiceRepository invoiceRepo;
  private final PlanAttemptRepository attemptRepo;

  /**
   * (기존) 결제 시도 기록 + 인보이스 상태 업데이트
   */
  @Transactional
  public void recordAttempt(
      Long piId,
      boolean success,
      String failReason,
      String respUid,
      String receiptUrl,
      String respJson
  ) {
    var inv = invoiceRepo.findById(piId).orElseThrow();

    var att = PlanAttemptEntity.builder()
        .invoice(inv)
        .pattResult(success ? PattResult.SUCCESS : PattResult.FAIL)
        .pattFail(success ? null : failReason)
        .pattUid(respUid)
        .pattUrl(receiptUrl)
        .pattResponse(respJson != null ? respJson : "{}")
        .build();
    attemptRepo.save(att);

    inv.setPiStat(success ? PiStatus.PAID : PiStatus.FAILED);
    if (success) inv.setPiPaid(LocalDateTime.now());
    invoiceRepo.save(inv);
  }

  /**
   * (신규) 호출부 호환용: 컨트롤러/서비스에서 사용 중인 시그니처
   * finishAttempt(...) == recordAttempt(...) 와 동일 동작
   */
  @Transactional
  public void finishAttempt(
      Long invoiceId,
      boolean success,
      String failReason,
      String paymentId,
      String receiptUrl,
      String rawJson
  ) {
    // 내부적으로 기존 메서드 재사용
    recordAttempt(invoiceId, success, failReason, paymentId, receiptUrl, rawJson);
  }
}
