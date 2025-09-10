// com.dodam.plan.controller.PlanPgProxyController
package com.dodam.plan.controller;

import com.dodam.plan.service.PlanPaymentGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pg")
public class PlanPgProxyController {

    private final PlanPaymentGatewayService pgSvc;

    // PortOne v2 결제 단건 조회 (paymentId 기반)
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable String paymentId) {
        var res = pgSvc.getPayment(paymentId); // 아래 서비스 추가
        return ResponseEntity.ok(res);
    }
}
