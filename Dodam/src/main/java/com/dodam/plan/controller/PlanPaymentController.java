// src/main/java/com/dodam/plan/controller/PlanPaymentController.java
package com.dodam.plan.controller;

import com.dodam.plan.service.PlanPaymentGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PlanPaymentController {

    private final PlanPaymentGatewayService gateway;
    private final ObjectMapper om = new ObjectMapper();

    /** 결제 승인(confirm) — body: { paymentId, amount } */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody Map<String, Object> body) {
        String paymentId = body.get("paymentId") != null ? body.get("paymentId").toString() : null;
        Long amount = null;
        if (body.get("amount") != null) {
            try { amount = Long.valueOf(String.valueOf(body.get("amount"))); } catch (NumberFormatException ignore) {}
        }

        if (paymentId == null || amount == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "paymentId/amount 누락"
            ));
        }

        var confirmRes = gateway.confirmPaymentRaw(paymentId, amount);
        String raw = confirmRes.rawJson() != null ? confirmRes.rawJson() : "{}";

        return ResponseEntity.ok(Map.of(
                "success", confirmRes.success(),
                "uid", confirmRes.uid(),
                "receiptUrl", confirmRes.receiptUrl(),
                "reason", confirmRes.failReason(),
                "raw", raw
        ));
    }
}
