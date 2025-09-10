package com.dodam.plan.controller;

import com.dodam.plan.repository.PlanInvoiceRepository;
import com.dodam.plan.service.PlanBillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/pg")
@RequiredArgsConstructor
public class PlanPgWebhookController {

    private final PlanInvoiceRepository invoiceRepo;
    private final PlanBillingService billingSvc;

    /**
     * PortOne 결제/환불 웹훅 수신
     */
    @PostMapping
    public ResponseEntity<?> handle(@RequestBody WebhookPayload body) {
        var invOpt = invoiceRepo.findByPiUid(body.paymentId());
        if (invOpt.isEmpty()) return ResponseEntity.ok().build(); // 없는 인보이스 → 무시

        var inv = invOpt.get();
        boolean success = "PAID".equalsIgnoreCase(body.status());

        billingSvc.recordAttempt(
                inv.getPiId(),
                success,
                success ? null : body.failReason(),
                body.transactionUid(),
                body.receiptUrl(),
                body.rawJson()
        );

        return ResponseEntity.ok().build();
    }

    public static record WebhookPayload(
            String paymentId,
            String status,
            String transactionUid,
            String receiptUrl,
            String failReason,
            String rawJson
    ) {}
}
