// src/main/java/com/dodam/plan/controller/PlanPgProxyController.java
package com.dodam.plan.controller;

import com.dodam.plan.service.PlanPaymentGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pg")
@Slf4j
public class PlanPgProxyController {

    private final PlanPaymentGatewayService pgSvc;
    private final ObjectMapper om = new ObjectMapper();

    /** (프론트 복귀 처리용) 안전 조회: txId 또는 paymentId 중 하나 */
    @GetMapping(value = "/lookup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> lookup(
            @RequestParam(required = false) String txId,
            @RequestParam(required = false) String paymentId
    ) {
        PlanPaymentGatewayService.PgLookupResult res = pgSvc.safeLookup(txId, paymentId);
        return ResponseEntity.ok(res);
    }
}
