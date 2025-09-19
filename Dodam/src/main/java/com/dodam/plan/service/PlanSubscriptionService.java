// src/main/java/com/dodam/plan/service/PlanSubscriptionService.java
package com.dodam.plan.service;

import com.dodam.plan.Entity.PlanInvoiceEntity;
import com.dodam.plan.dto.PlanSubscriptionStartReq;

import java.util.Map;

public interface PlanSubscriptionService {
    /** 인보이스가 결제 확정되었을 때 구독/인보이스 상태를 반영 */
    void activateInvoice(PlanInvoiceEntity invoice, int months);

    /**
     * (빌링키 즉시결제 → 폴링으로 확정)까지 한 번에 처리
     * @param invoiceId 인보이스 ID (PENDING 상태)
     * @param mid 세션의 회원 ID (mid)
     * @param termMonths 1,3,6,12 등
     * @return 결과 payload (status: PAID/FAILED/TIMEOUT, paymentId, receiptUrl 등)
     */
    Map<String, Object> chargeByBillingKeyAndConfirm(Long invoiceId, String mid, int termMonths);
    
    Map<String, Object> chargeAndConfirm(String mid, PlanSubscriptionStartReq req); // 결과 Map으로 응답
}
