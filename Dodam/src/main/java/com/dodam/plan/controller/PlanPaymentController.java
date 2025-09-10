// src/main/java/com/dodam/plan/controller/PlanPaymentController.java
package com.dodam.plan.controller;

import com.dodam.plan.repository.PlanInvoiceRepository;
import com.dodam.plan.service.PlanPaymentGatewayService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PlanPaymentController {

    private final PlanPaymentGatewayService gateway;
    private final PlanInvoiceRepository invoiceRepo;

    // 프론트에서 {invoiceId} 또는 {paymentId, amount} 아무거나 보내게 허용
    public record ConfirmReq(Long invoiceId, String paymentId, Long amount) {}

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody Map<String, Object> body) {
        String paymentId = body.get("paymentId") != null ? body.get("paymentId").toString() : null;
        Long amount = body.get("amount") != null ? Long.valueOf(body.get("amount").toString()) : null;
        Long invoiceId = body.get("invoiceId") != null ? Long.valueOf(body.get("invoiceId").toString()) : null;

        try {
            if (paymentId != null && amount != null) {
                var res = gateway.confirmPayment(paymentId, amount).block();
                return ResponseEntity.ok(Map.of("success", true, "status", res.getStatus()));
            } else if (invoiceId != null) {
                var inv = invoiceRepo.findById(invoiceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 invoiceId"));
                if (inv.getPiUid() == null || inv.getPiAmount() == null) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "청구서에 paymentId/amount가 없습니다."));
                }
                var res = gateway.confirmPayment(inv.getPiUid(), inv.getPiAmount().longValue()).block();
                return ResponseEntity.ok(Map.of("success", true, "status", res.getStatus()));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "paymentId+amount 또는 invoiceId 필요"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @PostMapping("/billing-keys")
    public Mono<Map<String, Object>> registerBillingKey(@RequestBody Map<String, Object> req) {
        return gateway.registerBillingKey(req)
            .map(res -> Map.of(
                "success", true,
                "billingKey", res.getBillingKey(),
                "customerId", res.getCustomer() != null ? res.getCustomer().getId() : null
            ));
    }
}
