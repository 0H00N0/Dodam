// src/main/java/com/dodam/plan/controller/PlanImmediateConfirmController.java
package com.dodam.plan.controller;

import com.dodam.plan.Entity.PlanInvoiceEntity;
import com.dodam.plan.Entity.PlanPaymentEntity;
import com.dodam.plan.enums.PlanEnums.PiStatus;
import com.dodam.plan.repository.PlanInvoiceRepository;
import com.dodam.plan.repository.PlanPaymentRepository;
import com.dodam.plan.service.PlanBillingService;
import com.dodam.plan.service.PlanPaymentGatewayService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PlanImmediateConfirmController {

    private final PlanPaymentRepository paymentRepo;
    private final PlanInvoiceRepository invoiceRepo;
    private final PlanPaymentGatewayService pgSvc;
    private final PlanBillingService billingSvc;

    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> confirm(@RequestBody Map<String, Object> body, HttpSession session) {
        String mid = (String) session.getAttribute("sid");
        if (!StringUtils.hasText(mid)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "LOGIN_REQUIRED"));
        }

        Long invoiceId = null;
        try {
            Object v = body.get("invoiceId");
            if (v instanceof Number n) invoiceId = n.longValue();
            else if (v != null) invoiceId = Long.parseLong(v.toString());
        } catch (Exception ignore) {}
        if (invoiceId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "MISSING_INVOICE_ID"));
        }

        Optional<PlanInvoiceEntity> optInv = invoiceRepo.findById(invoiceId);
        if (optInv.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "INVOICE_NOT_FOUND"));
        }
        PlanInvoiceEntity inv = optInv.get();

        // 최신 카드 한 장 선택: payId DESC
        PlanPaymentEntity card = paymentRepo.findTop1ByMidOrderByPayIdDesc(mid)
                .orElseGet(() -> {
                    List<PlanPaymentEntity> list = paymentRepo.findByMid(mid);
                    if (list == null || list.isEmpty()) return null;
                    return list.stream()
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparing(PlanPaymentEntity::getPayId, Comparator.nullsLast(Long::compareTo)).reversed())
                            .findFirst().orElse(null);
                });

        if (card == null || !StringUtils.hasText(card.getPayKey())) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(Map.of("error", "NO_BILLING_KEY"));
        }

        // PortOne v2 /instant 결제 (amount: BigDecimal → long)
        long amount = safeAmountToLong(inv.getPiAmount(), inv.getPiCurr());
        var res = pgSvc.payByBillingKey(card.getPayKey(), amount, card.getPayCustomer());
        boolean ok = res.success();

        // 시도 기록
        billingSvc.recordAttempt(
                inv.getPiId(),
                ok,
                res.failReason(),
                res.paymentId(),
                res.receiptUrl(),
                res.rawJson()
        );

        // 인보이스 상태 갱신
        inv.setPiStat(ok ? PiStatus.PAID : PiStatus.FAILED);
        if (ok) inv.setPiPaid(LocalDateTime.now());
        invoiceRepo.save(inv);

        if (ok) {
            return ResponseEntity.ok(Map.of(
                    "result", "PAID",
                    "paymentId", res.paymentId(),
                    "receiptUrl", res.receiptUrl()
            ));
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "result", "FAIL",
                "paymentId", res.paymentId(),
                "reason", res.failReason()
        ));
    }

    /** KRW는 소수 없음 → 정수부만 사용. 필요 시 통화별 처리 확장 */
    private long safeAmountToLong(BigDecimal bd, String currency) {
        if (bd == null) return 0L;
        // KRW 기본 가정: 소수점 반올림 제거
        return bd.setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
    }
}
