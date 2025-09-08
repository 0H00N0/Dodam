package com.dodam.plan.controller;

import com.dodam.plan.service.PlanPaymentGatewayService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@CrossOrigin( // 필요시 CORS 허용
        origins = "http://localhost:3000",
        allowCredentials = "true"
)
public class PlanPaymentController {

    private final PlanPaymentGatewayService gateway;

    public record ConfirmRequest(
            String invoiceId,  // 우리 인보이스 ID (없어도 됨)
            String paymentId   // PortOne 결제 ID (없어도 됨)
    ) {}

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmRequest req) {
        // invoiceId 또는 paymentId 중 하나만 있어도 확인 가능하게
        String id = (req.invoiceId() != null && !req.invoiceId().isBlank())
                ? req.invoiceId()
                : req.paymentId();

        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "invoiceId 또는 paymentId가 누락되었습니다."));
        }

        var res = gateway.getPayment(id); // PortOne 결제 조회 (아래 2번에 구현)
        if (res == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "결제 정보를 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(Map.of(
                "status",        res.status(),
                "paymentId",     res.paymentId(),
                "billingKey",    res.billingKey(),
                "customerId",    res.customerId(),
                "issuerName",    res.issuerName(),
                "bin",           res.bin(),
                "last4",         res.last4()
        ));
    }
}