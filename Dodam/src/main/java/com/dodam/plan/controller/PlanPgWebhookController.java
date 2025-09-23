// src/main/java/com/dodam/plan/controller/PlanPgWebhookController.java
package com.dodam.plan.controller;

import com.dodam.plan.Entity.PlanInvoiceEntity;
import com.dodam.plan.dto.PlanLookupResult;
import com.dodam.plan.repository.PlanInvoiceRepository;
import com.dodam.plan.service.PlanBillingService;
import com.dodam.plan.service.PlanPaymentGatewayService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/webhooks/pg")
@RequiredArgsConstructor
public class PlanPgWebhookController {

    private final PlanInvoiceRepository invoiceRepo;
    private final PlanBillingService billingSvc;
    private final PlanPaymentGatewayService pgSvc;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handle(@RequestBody Map<String, Object> body) {
        try {
            JsonMapper mapper = JsonMapper.builder().build();
            JsonNode root = mapper.valueToTree(body);

            // ★★★ 다양한 키에서 provider payment id 추출
            String paymentId = firstNonEmpty(
                    text(root, "paymentId"),
                    text(root, "id"),
                    text(root, "transactionUid"),
                    text(root, "payment", "id")
            );

            String statusRaw  = n(text(root, "status"));
            String receiptUrl = n(text(root, "receiptUrl"));
            if (receiptUrl == null) receiptUrl = n(text(root, "receipt", "url")); // 대체 경로
            String rawJson    = mapper.writeValueAsString(body);

            log.info("[WEBHOOK] recv paymentId={}, status={}, receipt={}", paymentId, statusRaw, receiptUrl);

            if (!StringUtils.hasText(paymentId)) {
                log.warn("[WEBHOOK] skip: empty payment id. body={}", rawJson);
                return ResponseEntity.ok().build(); // 재전송 방지
            }

            Optional<PlanInvoiceEntity> optInv = invoiceRepo.findByPiUid(paymentId);
            if (optInv.isEmpty()) {
                log.warn("[WEBHOOK] invoice not found by paymentId={}", paymentId);
                return ResponseEntity.ok().build(); // 못 찾아도 200
            }
            Long invoiceId = optInv.get().getPiId();

            // 1) 웹훅 본문 상태 우선
            String st = (statusRaw == null ? "" : statusRaw.trim().toUpperCase(Locale.ROOT));
            if (isPaid(st)) {
                // ✅ 즉시 확정 + rawJson으로 카드메타 저장
                billingSvc.recordAttempt(invoiceId, true, null, paymentId, receiptUrl, rawJson);
                return ResponseEntity.ok().build();
            }
            if (isFailed(st)) {
                billingSvc.recordAttempt(invoiceId, false, "WEBHOOK:" + st, paymentId, receiptUrl, rawJson);
                return ResponseEntity.ok().build();
            }

            // 2) 애매하면 lookup 보강(짧게)
            PlanLookupResult look = pgSvc.safeLookup(paymentId);
            String lst = look.status() == null ? "" : look.status().toUpperCase(Locale.ROOT);
            if (isPaid(lst)) {
                billingSvc.recordAttempt(invoiceId, true, null, paymentId, receiptUrl, look.rawJson());
            } else if (isFailed(lst)) {
                billingSvc.recordAttempt(invoiceId, false, "LOOKUP:" + lst, paymentId, receiptUrl, look.rawJson());
            } else {
                billingSvc.recordAttempt(invoiceId, false, "LOOKUP:PENDING", paymentId, receiptUrl, look.rawJson());
            }
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("[WEBHOOK] error: {}", e.toString(), e);
            return ResponseEntity.ok().build();
        }
    }

    // ==== helpers ====
    private static String text(JsonNode n, String... path) {
        JsonNode cur = n;
        for (String k : path) {
            if (cur == null) return null;
            cur = cur.get(k);
        }
        return (cur == null || cur.isNull()) ? null : cur.asText(null);
    }
    private static String n(String s) { return (s == null || s.isBlank()) ? null : s; }

    private static String firstNonEmpty(String... v) {
        for (String s : v) if (s != null && !s.isBlank()) return s;
        return null;
    }
    private boolean isPaid(String s) {
        if (s == null) return false;
        s = s.trim().toUpperCase(Locale.ROOT);
        return s.equals("PAID") || s.equals("SUCCEEDED") || s.equals("SUCCESS") || s.equals("PARTIAL_PAID");
    }
    private boolean isFailed(String s) {
        if (s == null) return false;
        s = s.trim().toUpperCase(Locale.ROOT);
        return s.equals("FAILED") || s.equals("CANCELED") || s.equals("CANCELLED");
    }
}
