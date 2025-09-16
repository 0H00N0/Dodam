// src/main/java/com/dodam/plan/controller/PlanPaymentController.java
package com.dodam.plan.controller;

import com.dodam.plan.enums.PlanEnums.PiStatus;
import com.dodam.plan.repository.PlanInvoiceRepository;
import com.dodam.plan.repository.PlanPaymentRepository;
import com.dodam.plan.service.PlanBillingService;
import com.dodam.plan.service.PlanPaymentGatewayService;
import com.dodam.plan.service.PlanPaymentGatewayService.PayResult;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PlanPaymentController {

    private final PlanInvoiceRepository invoiceRepo;
    private final PlanPaymentRepository paymentRepo;
    private final PlanPaymentGatewayService pgSvc;
    private final PlanBillingService billingSvc;

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody Map<String, Object> body, HttpSession session) {
        String mid = (String) session.getAttribute("sid");
        if (!StringUtils.hasText(mid)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "LOGIN_REQUIRED"));
        }
        Long invoiceId = ((Number) body.getOrDefault("invoiceId", 0)).longValue();

        try {
            var inv = invoiceRepo.findById(invoiceId).orElseThrow();
            if (inv.getPiStat() == PiStatus.PAID) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "paymentId", inv.getPiUid(),
                        "invoiceId", inv.getPiId(),
                        "message", "ALREADY_PAID"
                ));
            }

            var payment = paymentRepo.findTopByMidOrderByPayIdDesc(mid)
                    .orElseThrow(() -> new IllegalStateException("NO_PAYMENT_PROFILE"));

            long amount = toLong(inv.getPiAmount());

            var res = pgSvc.payByBillingKey(
                    inv.getPiUid(),              // paymentId
                    payment.getPayKey(),         // billingKey
                    amount,                      // total
                    payment.getPayCustomer()     // customerId
            );

            billingSvc.recordAttempt(
                    inv.getPiId(),
                    res.success(),
                    res.failReason(),
                    res.paymentId(),
                    res.receiptUrl(),
                    res.rawJson()
            );

            if (res.success()) {
                inv.setPiStat(PiStatus.PAID);
                invoiceRepo.save(inv);
            }

            return ResponseEntity.ok(Map.of(
                    "success", res.success(),
                    "invoiceId", inv.getPiId(),
                    "paymentId", res.paymentId(),
                    "receiptUrl", res.receiptUrl(),
                    "message", res.success() ? "CONFIRMED" : "PAY_FAILED"
            ));

        } catch (Exception ex) {
            log.error("CONFIRM_FAILED invoiceId={}, mid={}, ex={}", invoiceId, mid, ex.toString(), ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("success", false, "message","CONFIRM_FAILED"));
        }
    }

    private long toLong(BigDecimal bd) {
        return bd == null ? 0L : bd.setScale(0, java.math.RoundingMode.DOWN).longValueExact();
    }
}
