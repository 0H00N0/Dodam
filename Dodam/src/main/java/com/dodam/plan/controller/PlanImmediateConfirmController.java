package com.dodam.plan.controller;

import com.dodam.plan.service.PlanBillingService;
import com.dodam.plan.service.PlanPaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PlanImmediateConfirmController {

    private final PlanPaymentGatewayService pgSvc;
    private final PlanBillingService billingSvc;

    public record ConfirmReq(String paymentId, Long amount, Long invoiceId) {}

    @PostMapping("/confirm-by-payment-id")
    public Map<String, Object> confirmByPaymentId(@RequestBody ConfirmReq req) {
        if (!StringUtils.hasText(req.paymentId()) || req.amount()==null)
            throw new IllegalArgumentException("paymentId and amount are required");

        var res = pgSvc.confirmPaymentRaw(req.paymentId(), req.amount());
        if (req.invoiceId()!=null) {
            billingSvc.recordAttempt(
                    req.invoiceId(),
                    res.success(),
                    res.failReason(),
                    res.uid(),
                    res.receiptUrl(),
                    res.rawJson()!=null? res.rawJson() : "{}"
            );
        }
        return Map.of(
                "result", res.success()?"ok":"fail",
                "uid", res.uid(),
                "receiptUrl", res.receiptUrl(),
                "reason", res.failReason()
        );
    }
}
