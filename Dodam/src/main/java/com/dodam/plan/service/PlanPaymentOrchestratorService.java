// src/main/java/com/dodam/plan/service/PlanPaymentOrchestratorService.java
package com.dodam.plan.service;

import com.dodam.plan.Entity.PlanInvoiceEntity;
import com.dodam.plan.Entity.PlanPaymentEntity;
import com.dodam.plan.repository.PlanInvoiceRepository;
import com.dodam.plan.repository.PlanPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanPaymentOrchestratorService {

    private final PlanPaymentGatewayService pgSvc;
    private final PlanBillingService billingSvc;          // recordAttempt(...) 제공
    private final PlanInvoiceRepository invoiceRepo;
    private final PlanPaymentRepository paymentRepo;

    /**
     * 배치/재시도: 인보이스 ID만으로 결제 시도
     */
    @Transactional
    public void tryPayInvoice(Long piId) {
        var inv = invoiceRepo.findById(piId)
                .orElseThrow(() -> new IllegalArgumentException("invoice not found: " + piId));

        // ✅ mid는 인보이스에 연결된 PlanMember에서 가져옵니다.
        String mid = extractMidFromInvoice(inv);

        // 사용자 최신 결제 프로필로 결제 시도
        PlanPaymentEntity payment = paymentRepo.findTopByMidOrderByPayIdDesc(mid)
                .orElseThrow(() -> new IllegalStateException("no payment profile for mid=" + mid));

        confirmInvoice(inv, payment);
    }

    /**
     * 인보이스 단건 승인(or 재시도)
     */
    @Transactional
    public void confirmInvoice(PlanInvoiceEntity inv, PlanPaymentEntity payment) {
        log.info("[confirmInvoice] invId={}, piUid={}, amount={}, mid={}",
                inv.getPiId(), inv.getPiUid(), inv.getPiAmount(), payment.getMid());

        var res = pgSvc.payWithBillingKey(
                inv.getPiUid(),                   // 내부 uid
                payment.getPayCustomer(),         // (== mid)
                payment.getPayKey(),
                inv.getPiAmount().longValue()
        );

        String uid      = res.uid();
        String reason   = res.failReason();
        String receipt  = res.receiptUrl();
        boolean success = res.success();
        String rawJson  = (res.rawJson() != null) ? res.rawJson() : "{}";

        billingSvc.recordAttempt(
                inv.getPiId(),
                success,
                reason,
                uid,
                receipt,
                rawJson
        );

        log.info("[confirmInvoice] result: success={}, uid={}, reason={}", success, uid, reason);
    }

    /**
     * 인보이스에서 회원 MID 추출
     * - 기본: inv.getPlanMember().getMid()
     * - 만약 PlanMember에 getMid()가 없고 Member 엔티티를 들고 있다면 아래 한 줄을 바꾸세요:
     *     return inv.getPlanMember().getMember().getMid();
     */
    private String extractMidFromInvoice(PlanInvoiceEntity inv) {
        if (inv.getPlanMember() == null) {
            throw new IllegalStateException("invoice has no PlanMember linked");
        }
        // 🔽 프로젝트 모델에 맞게 한 줄만 선택해서 사용하세요.
        return inv.getPlanMember().getMember().getMid();
        // return inv.getPlanMember().getMember().getMid(); // <-- PlanMember 가 Member 엔티티를 통해 mid를 가질 때
    }
}
