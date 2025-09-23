// src/main/java/com/dodam/plan/service/PlanBillingService.java
package com.dodam.plan.service;

import com.dodam.plan.Entity.PlanAttemptEntity;
import com.dodam.plan.Entity.PlanInvoiceEntity;
import com.dodam.plan.Entity.PlanPaymentEntity;
import com.dodam.plan.dto.PlanCardMeta;
import com.dodam.plan.enums.PlanEnums.PattResult;
import com.dodam.plan.enums.PlanEnums.PiStatus;
import com.dodam.plan.repository.PlanAttemptRepository;
import com.dodam.plan.repository.PlanInvoiceRepository;
import com.dodam.plan.repository.PlanPaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanBillingService {

    private final PlanAttemptRepository attemptRepo;
    private final PlanInvoiceRepository invoiceRepo;
    private final PlanPaymentRepository paymentRepo;
    private final PlanPaymentGatewayService pgSvc;

    /**
     * 결제 시도 기록 + 인보이스 상태 전이(멱등 규칙) + 카드 메타 저장
     */
    @Transactional
    public void recordAttempt(Long invoiceId,
                              boolean success,
                              String failReason,
                              String respUid,
                              String receiptUrl,
                              String respJson) {

        PlanInvoiceEntity inv = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("INVOICE_NOT_FOUND:" + invoiceId));

        // 1) 시도 기록(항상 남김)
        PlanAttemptEntity att = PlanAttemptEntity.builder()
                .invoice(inv)
                .pattResult(success ? PattResult.SUCCESS : PattResult.FAIL)
                .pattFail(success ? null : failReason)
                .pattUid(respUid)
                .pattUrl(receiptUrl)
                .pattResponse(respJson)
                .build();
        attemptRepo.save(att);

        // 2) 인보이스 기본 정보 갱신(멱등 키)
        if (StringUtils.hasText(respUid)) {
            // ✅ piUid는 한 번만/한 군데만 바인딩: 이미 다른 인보이스가 소유 중이면 스킵
            invoiceRepo.findByPiUid(respUid).ifPresentOrElse(owner -> {
                if (!owner.getPiId().equals(invoiceId)) {
                    log.warn("[Billing] paymentId {} is already bound to invoice {}. skip binding to {}",
                            respUid, owner.getPiId(), invoiceId);
                } // owner == inv 인 경우는 그대로 유지(덮어쓸 필요 없음)
            }, () -> {
                inv.setPiUid(respUid); // 최초 바인딩
            });
        }

        // 3) 상태 전이 규칙
        if (success) {
            inv.setPiStat(PiStatus.PAID);
            inv.setPiPaid(LocalDateTime.now());
        } else {
            String reason = (failReason == null ? "" : failReason).toUpperCase(Locale.ROOT).trim();
            if (reason.startsWith("LOOKUP:FAILED") || reason.startsWith("LOOKUP:CANCELED")) {
                inv.setPiStat(PiStatus.FAILED);
            } else {
                if (inv.getPiStat() == null || inv.getPiStat() == PiStatus.FAILED) {
                    inv.setPiStat(PiStatus.PENDING);
                }
            }
        }
        invoiceRepo.save(inv);

     // 4) (중요) 카드 메타 저장 — 성공 확정일 때 rawJson에서 추출
     // src/main/java/com/dodam/plan/service/PlanBillingService.java
     // ...중략...
     try {
         PlanPaymentEntity payment = inv.getPlanMember() != null ? inv.getPlanMember().getPayment() : null;
         if (success && payment != null && StringUtils.hasText(respJson)) {
             PlanCardMeta meta = pgSvc.extractCardMeta(respJson);
             if (meta != null) {
                 log.info("[Billing] will save cardMeta paymentId={}, bin={}, brand={}, last4={}, pg={}",
                         payment.getPayId(), meta.getBin(), meta.getBrand(), meta.getLast4(), meta.getPg());

                 int updated = paymentRepo.updateCardMeta(
                         payment.getPayId(),
                         safe(meta.getBin()),
                         safe(meta.getBrand()),
                         safe(meta.getLast4()),
                         safe(meta.getPg())
                 );
                 log.info("[Billing] cardMeta update rows={}", updated);

                 payment.setPayRaw(respJson); // 최근 raw 보관(선택)
                 paymentRepo.save(payment);
             }
         }
     } catch (Exception e) {
         log.warn("[Billing] save card meta failed: {}", e.toString());
     }
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }
}
