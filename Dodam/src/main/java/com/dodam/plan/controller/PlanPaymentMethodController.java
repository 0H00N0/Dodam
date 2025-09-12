// src/main/java/com/dodam/plan/controller/PlanPaymentMethodController.java
package com.dodam.plan.controller;

import com.dodam.plan.dto.PlanPaymentRegisterReq;
import com.dodam.plan.Entity.PlanPaymentEntity;
import com.dodam.plan.repository.PlanPaymentRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/billing-keys")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PlanPaymentMethodController {

    private final PlanPaymentRepository paymentRepo;
    
    @GetMapping("/customer-id")
    public ResponseEntity<?> customerId(HttpSession session) {
        String mid = (String) session.getAttribute("sid");
        if (!StringUtils.hasText(mid)) {
            // 500 대신 명시적으로 401을 리턴
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "LOGIN_REQUIRED"));
        }

        String customerId = "CUST-" + mid;
        return ResponseEntity.ok(Map.of("customerId", customerId));
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody PlanPaymentRegisterReq req, HttpSession session) {
    	String mid = (String) session.getAttribute("sid");
        if (!StringUtils.hasText(mid)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","LOGIN_REQUIRED"));
        }
        // billingKey 필수 + 최소 길이 검증 (샌드박스도 보통 16자 이상)
        if (!StringUtils.hasText(req.billingKey()) || req.billingKey().trim().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error","MISSING_OR_INVALID_BILLING_KEY"));
        }

        String customerId = "CUST-" + mid;

        // upsert
        PlanPaymentEntity entity = paymentRepo.findByPayCustomer(customerId)
                .orElseGet(() -> PlanPaymentEntity.builder()
                        .mid(mid)
                        .payCustomer(customerId)
                        .build());

        entity.setPayKey(req.billingKey());
        entity.setPayPg(req.pg());
        entity.setPayBrand(req.brand());
        entity.setPayBin(req.bin());
        entity.setPayLast4(req.last4());
        entity.setPayRaw(req.raw());

        try {
            paymentRepo.save(entity);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("DUPLICATE");
        } catch (Exception e) {
            log.error("REGISTER_BILLING_KEY_FAIL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("INTERNAL_ERROR");
        }

        return ResponseEntity.ok(Map.of(
                "result","ok",
                "payId", entity.getPayId(),
                "customerId", entity.getPayCustomer(),
                "pg", entity.getPayPg(),
                "brand", entity.getPayBrand(),
                "bin", entity.getPayBin(),
                "last4", entity.getPayLast4()
        ));
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(HttpSession session) {
        String mid = (String) session.getAttribute("sid");
        if (!StringUtils.hasText(mid)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("LOGIN_REQUIRED");
        }
        List<PlanPaymentEntity> list = paymentRepo.findAllByMid(mid);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpSession session) {
        String mid = (String) session.getAttribute("sid");
        if (!StringUtils.hasText(mid)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED");
        }
        PlanPaymentEntity target = paymentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NOT_FOUND"));
        if (!mid.equals(target.getMid())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }
        paymentRepo.delete(target);
        return ResponseEntity.ok(Map.of("result","ok"));
    }
}