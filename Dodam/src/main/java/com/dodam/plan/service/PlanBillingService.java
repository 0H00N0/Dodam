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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanBillingService {

    private final PlanAttemptRepository attemptRepo;
    private final PlanInvoiceRepository invoiceRepo;
    private final PlanPaymentRepository paymentRepo;
    private final PlanPaymentGatewayService pgSvc;

    private static final ObjectMapper OM = new ObjectMapper();

    @Transactional
    public void recordAttempt(Long invoiceId,
                              boolean success,
                              String failReason,
                              String respUid,
                              String receiptUrl,
                              String respJson) {

        PlanInvoiceEntity inv = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("INVOICE_NOT_FOUND:" + invoiceId));

        // 1) 시도 기록
        PlanAttemptEntity att = PlanAttemptEntity.builder()
                .invoice(inv)
                .pattResult(success ? PattResult.SUCCESS : PattResult.FAIL)
                .pattFail(success ? null : failReason)
                .pattUid(respUid)   // provider payment id or orderId
                .pattUrl(receiptUrl)
                .pattResponse(respJson)
                .build();
        attemptRepo.save(att);

        // 2) piUid 멱등 바인딩
        if (StringUtils.hasText(respUid)) {
            invoiceRepo.findByPiUid(respUid).ifPresentOrElse(owner -> {
                if (!owner.getPiId().equals(invoiceId)) {
                    log.warn("[Billing] paymentId {} is already bound to invoice {}. skip binding to {}",
                            respUid, owner.getPiId(), invoiceId);
                }
            }, () -> inv.setPiUid(respUid));
        }

        // 3) 상태 전이
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

     // 4) 카드 메타 저장 (반드시 billingKey로 매칭된 카드에만)
        try {
            if (success && StringUtils.hasText(respJson)) {
                String usedBillingKey = extractBillingKey(respJson);

                if (!StringUtils.hasText(usedBillingKey)) {
                    log.info("[Billing] skip card meta: no billingKey in provider response (invoice={})", invoiceId);
                    return;
                }

                Optional<PlanPaymentEntity> byKey = paymentRepo.findByPayKey(usedBillingKey);
                if (byKey.isEmpty()) {
                    log.warn("[Billing] skip card meta: payment not found by billingKey={} (invoice={})",
                            usedBillingKey, invoiceId);
                    return;
                }

                PlanPaymentEntity targetPayment = byKey.get();

                PlanCardMeta meta = pgSvc.extractCardMeta(respJson);
                boolean hasAny = meta != null && (
                        StringUtils.hasText(meta.getBin()) ||
                        StringUtils.hasText(meta.getBrand()) ||
                        StringUtils.hasText(meta.getLast4()) ||
                        StringUtils.hasText(meta.getPg())
                );
                if (hasAny) {
                    int updated = paymentRepo.updateCardMeta(
                            targetPayment.getPayId(),
                            safe(meta.getBin()),
                            safe(meta.getBrand()),
                            safe(meta.getLast4()),
                            safe(meta.getPg())
                    );
                    log.info("[Billing] cardMeta update rows={} (payId={}, bk={})",
                            updated, targetPayment.getPayId(), targetPayment.getPayKey());
                } else {
                    log.info("[Billing] cardMeta skipped (no fields) for paymentId={}", targetPayment.getPayId());
                }

                targetPayment.setPayRaw(respJson);
                paymentRepo.save(targetPayment);
            }
        } catch (Exception e) {
            log.warn("[Billing] save card meta failed: {}", e.toString(), e);
        }
    }

    /** raw JSON 에서 billingKey 추출 (items[0] / payment / root 모두 지원) */
    private String extractBillingKey(String raw) {
        try {
            JsonNode root = OM.readTree(raw);
            // items[0].billingKey
            if (root.has("items") && root.get("items").isArray() && root.get("items").size() > 0) {
                String v = n(root.get("items").get(0).path("billingKey").asText(null));
                if (StringUtils.hasText(v)) return v;
            }
            // payment.billingKey
            String v2 = n(root.path("payment").path("billingKey").asText(null));
            if (StringUtils.hasText(v2)) return v2;
            // root.billingKey
            String v3 = n(root.path("billingKey").asText(null));
            if (StringUtils.hasText(v3)) return v3;
        } catch (Exception ignore) { }
        return null;
    }

    private String safe(String v) { return (v == null || v.isBlank()) ? null : v; }
    private static String n(String s){ return (s==null || s.isBlank()) ? null : s; }
}
