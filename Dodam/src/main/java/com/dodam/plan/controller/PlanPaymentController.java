package com.dodam.plan.controller;

import com.dodam.plan.service.PlanPaymentOrchestratorService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PlanPaymentController {

    private final PlanPaymentOrchestratorService orchestrator;

    /**
     * 특정 Invoice 결제 승인 (세션 기반)
     */
    @PostMapping("/confirm/{invoiceId}")
    public ResponseEntity<?> confirm(HttpSession session, @PathVariable Long invoiceId) {
        Long mnum = (Long) session.getAttribute("sid");
        if (mnum == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        orchestrator.tryPayInvoiceForMember(invoiceId, mnum);
        return ResponseEntity.ok().build();
    }
}
