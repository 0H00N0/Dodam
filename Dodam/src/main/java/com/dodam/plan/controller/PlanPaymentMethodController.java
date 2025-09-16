package com.dodam.plan.controller;

import com.dodam.plan.dto.PlanPaymentRegisterReq;
import com.dodam.plan.dto.PlanCardMeta;
import com.dodam.plan.Entity.PlanPaymentEntity;
import com.dodam.plan.repository.PlanPaymentRepository;
import com.dodam.plan.service.PlanPaymentProfileService;
import com.dodam.plan.service.PlanPaymentGatewayService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/billing-keys")
public class PlanPaymentMethodController {

    private final PlanPaymentRepository paymentRepo;
    private final PlanPaymentProfileService profileSvc;
    private final PlanPaymentGatewayService pgSvc;

    /** 카드 목록 */
    @GetMapping({"/list", ""})
    public ResponseEntity<?> list(HttpSession session) {
        final String mid = (String) session.getAttribute("sid");
        if (!StringUtils.hasText(mid)) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", "LOGIN_REQUIRED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
        return ResponseEntity.ok(paymentRepo.findByMid(mid));
    }

    /** 카드 등록(멱등) */
    @PostMapping(value="/register", consumes=MediaType.APPLICATION_JSON_VALUE)
    @Transactional(noRollbackFor = DataIntegrityViolationException.class)
    public ResponseEntity<?> register(@RequestBody PlanPaymentRegisterReq req, HttpSession session) {
        final String mid = (String) session.getAttribute("sid");
        final String key = req.getBillingKey() == null ? null : req.getBillingKey().trim();

        log.info("[billing-keys/register] sid={}, billingKey={}", mid, key);

        if (!StringUtils.hasText(mid)) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", "LOGIN_REQUIRED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
        if (!StringUtils.hasText(key)) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", "MISSING_BILLING_KEY");
            return ResponseEntity.badRequest().body(body);
        }

        // 0) 메타 추출 (rawJson 우선)
        PlanCardMeta meta = null;
        try { meta = pgSvc.extractCardMeta(req.getRawJson()); } catch (Exception ignore) {}
        final String pg    = firstNonBlank(meta != null ? meta.getPg()    : null, safe(req.getPg()));
        final String brand = firstNonBlank(meta != null ? meta.getBrand() : null, safe(req.getBrand()));
        final String bin   = firstNonBlank(meta != null ? meta.getBin()   : null, safe(req.getBin()));
        final String last4 = firstNonBlank(meta != null ? meta.getLast4() : null, safe(req.getLast4()));
        final String raw   = req.getRawJson();

        // 1) 선조회 멱등
        var existingOpt = paymentRepo.findByPayKey(key);
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();

            if (!Objects.equals(existing.getMid(), mid)) {
                Map<String, Object> body = new HashMap<>();
                body.put("error", "OWNED_BY_ANOTHER_USER");
                body.put("billingKey", key);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
            }

            boolean changed = false;
            if (!StringUtils.hasText(existing.getPayPg())    && StringUtils.hasText(pg))    { existing.setPayPg(pg); changed = true; }
            if (!StringUtils.hasText(existing.getPayBrand()) && StringUtils.hasText(brand)) { existing.setPayBrand(brand); changed = true; }
            if (!StringUtils.hasText(existing.getPayBin())   && StringUtils.hasText(bin))   { existing.setPayBin(bin); changed = true; }
            if (!StringUtils.hasText(existing.getPayLast4()) && StringUtils.hasText(last4)) { existing.setPayLast4(last4); changed = true; }
            if (!StringUtils.hasText(existing.getPayRaw())   && StringUtils.hasText(raw))   { existing.setPayRaw(raw); changed = true; }
            if (changed) paymentRepo.save(existing);

            Map<String, Object> body = new HashMap<>();
            body.put("message", "ALREADY_REGISTERED");
            body.put("billingKey", existing.getPayKey());
            if (existing.getPayPg() != null) body.put("pg", existing.getPayPg());
            if (existing.getPayBrand() != null) body.put("brand", existing.getPayBrand());
            if (existing.getPayBin() != null) body.put("bin", existing.getPayBin());
            if (existing.getPayLast4() != null) body.put("last4", existing.getPayLast4());
            return ResponseEntity.ok(body);
        }

        // 2) 신규 저장
        var entity = new PlanPaymentEntity();
        entity.setMid(mid);
        entity.setPayCustomer("cust_" + mid);
        entity.setPayKey(key);
        entity.setPayPg(pg);
        entity.setPayBrand(brand);
        entity.setPayBin(bin);
        entity.setPayLast4(last4);
        entity.setPayRaw(raw);

        try {
            paymentRepo.save(entity);
        } catch (DataIntegrityViolationException e) {
            var exist = paymentRepo.findByPayKey(key).orElse(null);
            if (exist != null) {
                if (!Objects.equals(exist.getMid(), mid)) {
                    Map<String, Object> body = new HashMap<>();
                    body.put("error", "OWNED_BY_ANOTHER_USER");
                    body.put("billingKey", key);
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
                }
                boolean changed = false;
                if (!StringUtils.hasText(exist.getPayPg())    && StringUtils.hasText(pg))    { exist.setPayPg(pg); changed = true; }
                if (!StringUtils.hasText(exist.getPayBrand()) && StringUtils.hasText(brand)) { exist.setPayBrand(brand); changed = true; }
                if (!StringUtils.hasText(exist.getPayBin())   && StringUtils.hasText(bin))   { exist.setPayBin(bin); changed = true; }
                if (!StringUtils.hasText(exist.getPayLast4()) && StringUtils.hasText(last4)) { exist.setPayLast4(last4); changed = true; }
                if (!StringUtils.hasText(exist.getPayRaw())   && StringUtils.hasText(raw))   { exist.setPayRaw(raw); changed = true; }
                if (changed) paymentRepo.save(exist);

                Map<String, Object> body = new HashMap<>();
                body.put("message", "ALREADY_REGISTERED");
                body.put("billingKey", exist.getPayKey());
                if (exist.getPayPg() != null) body.put("pg", exist.getPayPg());
                if (exist.getPayBrand() != null) body.put("brand", exist.getPayBrand());
                if (exist.getPayBin() != null) body.put("bin", exist.getPayBin());
                if (exist.getPayLast4() != null) body.put("last4", exist.getPayLast4());
                return ResponseEntity.ok(body);
            }
            throw e;
        }

        // 3) 프로필 upsert
        PlanPaymentRegisterReq req2 = new PlanPaymentRegisterReq();
        req2.setCustomerId(entity.getPayCustomer());
        req2.setBillingKey(entity.getPayKey());
        req2.setPg(entity.getPayPg());
        req2.setBrand(entity.getPayBrand());
        req2.setBin(entity.getPayBin());
        req2.setLast4(entity.getPayLast4());
        req2.setRawJson(entity.getPayRaw());

        profileSvc.upsert(mid, req2);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "REGISTERED");
        body.put("billingKey", entity.getPayKey());
        if (entity.getPayPg() != null) body.put("pg", entity.getPayPg());
        if (entity.getPayBrand() != null) body.put("brand", entity.getPayBrand());
        if (entity.getPayBin() != null) body.put("bin", entity.getPayBin());
        if (entity.getPayLast4() != null) body.put("last4", entity.getPayLast4());
        return ResponseEntity.ok(body);
    }

    private static String safe(String s) {
        return s == null ? null : s.trim();
    }
    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }

    /** 고객 ID 계산 */
    @PostMapping("/customer-id")
    public ResponseEntity<?> customerId(HttpSession session) {
        final String mid = (String) session.getAttribute("sid");
        if (!StringUtils.hasText(mid)) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", "LOGIN_REQUIRED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("customerId", "cust_" + mid);
        return ResponseEntity.ok(body);
    }
}
