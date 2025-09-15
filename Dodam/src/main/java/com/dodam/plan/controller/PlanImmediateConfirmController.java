package com.dodam.plan.controller;

import com.dodam.plan.Entity.PlanInvoiceEntity;
import com.dodam.plan.repository.PlanInvoiceRepository;
import com.dodam.plan.service.PlanBillingService;
import com.dodam.plan.service.PlanPaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PlanImmediateConfirmController {

    private final PlanInvoiceRepository invoiceRepo;
    private final PlanPaymentGatewayService gateway;   // payByBillingKey(...) 제공
    private final PlanBillingService billingSvc;       // finishAttempt(...) 제공

    /**
     * { invoiceId } 로 결제 승인
     * - invoice → planMember → payment 에서 billingKey/customerId/amount 추출
     * - PG 승인 시도 → 시도 결과/원문 기록 → 상태 갱신
     */
    @PostMapping("/confirm")
    public Map<String, Object> confirm(@RequestBody Map<String, Object> body) {
        Object raw = body.get("invoiceId");
        if (raw == null) throw new IllegalArgumentException("MISSING_INVOICE_ID");
        Long invoiceId = (raw instanceof Number) ? ((Number) raw).longValue() : Long.parseLong(raw.toString());

        PlanInvoiceEntity inv = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("INVOICE_NOT_FOUND"));

        var pm = inv.getPlanMember();
        var pay = pm.getPayment();

        String billingKey   = pay.getPayKey();       // 저장해둔 빌링키
        String customerId   = pay.getPayCustomer();  // PortOne customerId
        BigDecimal amount   = inv.getPiAmount();

        if (!StringUtils.hasText(billingKey) || !StringUtils.hasText(customerId)) {
            throw new IllegalStateException("PAYMENT_PROFILE_INCOMPLETE");
        }

        var res = gateway.payByBillingKey(billingKey, amount.longValue(), customerId);

        // 시도/최종 상태 기록
        billingSvc.finishAttempt(
                inv.getPiId(),
                res.success(),
                res.failReason(),
                res.paymentId(),
                res.receiptUrl(),
                res.rawJson() != null ? res.rawJson() : "{}"
        );

        return Map.of(
                "result", res.success() ? "ok" : "fail",
                "uid", res.paymentId(),
                "receiptUrl", res.receiptUrl(),
                "reason", res.failReason()
        );
    }
}
