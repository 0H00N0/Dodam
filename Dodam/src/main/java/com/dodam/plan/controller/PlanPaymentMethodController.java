package com.dodam.plan.controller;

import com.dodam.plan.dto.PlanCardMeta;
import com.dodam.plan.dto.PlanPaymentRegisterReq;
import com.dodam.plan.Entity.PlanPaymentEntity; // ← 네가 쓰는 대문자 Entity 패키지에 맞춤
import com.dodam.plan.repository.PlanPaymentRepository;
import com.dodam.plan.service.PlanPaymentGatewayService;
import com.dodam.plan.service.PlanPaymentProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/billing-keys")
public class PlanPaymentMethodController {

    private final PlanPaymentRepository paymentRepo;
    private final PlanPaymentProfileService profileSvc;
    private final PlanPaymentGatewayService pgSvc;

    // ===== 1) 카드 목록 (프런트는 배열 기대) =====
    @GetMapping("/list")
    public ResponseEntity<?> list(HttpSession session) {
        String mid = (String) session.getAttribute("sid");
        if (!StringUtils.hasText(mid)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","LOGIN_REQUIRED"));
        }
        var list = paymentRepo.findAllByMid(mid);
        var arr = list.stream().map(PlanPaymentMethodController::toMap).toList();
        return ResponseEntity.ok(arr);
    }

    // ===== 2) 카드 등록 (빌링키 저장) =====
    // @Transactional 제거: 리포지토리 레벨에서 트랜잭션 수행 (UnexpectedRollback 회피)
    @PostMapping(value="/register", consumes=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@RequestBody PlanPaymentRegisterReq req, HttpSession session) {
        String mid = (String) session.getAttribute("sid");
        if (!StringUtils.hasText(mid)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","LOGIN_REQUIRED"));
        }

        String billingKey = StringUtils.trimAllWhitespace(req.getBillingKey());
        if (!StringUtils.hasText(billingKey)) {
            return ResponseEntity.badRequest().body(Map.of("error","MISSING_BILLING_KEY"));
        }

        // 1) 선검증: 동일 payKey 존재 여부
        var existingOpt = paymentRepo.findByPayKey(billingKey);
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            if (!Objects.equals(existing.getMid(), mid)) {
                // 타인 소유
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","OWNED_BY_ANOTHER_USER"));
            }
            // 내 카드 → 메타 보강 후 OK
            safeMergeMeta(existing, safeExtract(req.getRawJson()));
            if (!StringUtils.hasText(existing.getPayRaw()) && StringUtils.hasText(req.getRawJson())) {
                existing.setPayRaw(req.getRawJson());
            }
            try {
                paymentRepo.save(existing);
            } catch (Exception e) {
                log.warn("update meta failed but keeping existing card. {}", e.toString());
                // 메타 보강 실패해도 기존 등록은 유지하므로 OK
            }
            return ResponseEntity.ok("ALREADY_REGISTERED");
        }

        // 2) 복합 유니크 방지 (mid+key) — 제약 위반 사전 차단
        if (paymentRepo.existsByMidAndPayKey(mid, billingKey)) {
            return ResponseEntity.ok("ALREADY_REGISTERED");
        }

        // 3) 신규 저장 (메타/RAW 비어도 OK) + PAYCUSTOMER NOT NULL 충족
        PlanCardMeta meta = safeExtract(req.getRawJson());

        // PortOne customerId 확보 시도 (없으면 mid로 대체)
        String customerId = null;
        try {
            // 필요 시 실제 customerId 확보 로직 연결 (존재할 경우)
            // customerId = profileSvc.ensureCustomerId(mid);
        } catch (Exception ignore) {}
        if (!StringUtils.hasText(customerId)) {
            customerId = mid; // 🔴 DB NOT NULL 충족을 위해 최소 mid 사용
        }

        PlanPaymentEntity e = PlanPaymentEntity.builder()
                .mid(mid)
                .payKey(billingKey)
                .payCustomer(customerId)                // 🔴 핵심: NOT NULL 방지
                .payCreatedAt(LocalDateTime.now())
                .payRaw(req.getRawJson())
                .build();

        safeMergeMeta(e, meta);

        try {
            paymentRepo.save(e);
            return ResponseEntity.ok(toMap(e));
        } catch (DataIntegrityViolationException dup) {
            String msg = String.valueOf(dup.getMostSpecificCause());
            // 제약 위반 메시지에 따라 분기
            if (msg != null && msg.contains("UK_PLANPAYMENT_MID_KEY")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","DUPLICATE_BILLING_KEY"));
            }
            if (msg != null && msg.contains("ORA-01400") && msg.contains("PAYCUSTOMER")) {
                return ResponseEntity.badRequest().body(Map.of("error","MISSING_PAYCUSTOMER"));
            }
            log.warn("register constraint violation: {}", msg);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error","CONSTRAINT_VIOLATION"));
        } catch (Exception ex) {
            log.error("REGISTER FAIL mid={} key={} rawLen={} ex={}",
                    mid, billingKey, (req.getRawJson()==null?0:req.getRawJson().length()), ex.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","INTERNAL_SERVER_ERROR"));
        }
    }

    // ===== helpers =====

    private static Map<String,Object> toMap(PlanPaymentEntity e) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", e.getPayId());
        m.put("billingKey", e.getPayKey());
        m.put("brand", e.getPayBrand());
        m.put("bin", e.getPayBin());
        m.put("last4", e.getPayLast4());
        m.put("pg", e.getPayPg());
        m.put("createdAt", e.getPayCreatedAt()==null? null : e.getPayCreatedAt().toString());
        return m;
    }

    private PlanCardMeta safeExtract(String rawJson) {
        try {
            return pgSvc.extractCardMeta(rawJson);
        } catch (Exception ex) {
            log.warn("extractCardMeta failed, continue without meta: {}", ex.toString());
            return new PlanCardMeta(null,null,null,null);
        }
    }

    // ⚠️ PlanCardMeta 가 record 이므로 brand()/bin()/last4()/pg() 접근자 사용
    private void safeMergeMeta(PlanPaymentEntity e, PlanCardMeta meta) {
        if (meta == null) return;
        if (!StringUtils.hasText(e.getPayBrand()) && StringUtils.hasText(meta.getBrand())) e.setPayBrand(meta.getBrand());
        if (!StringUtils.hasText(e.getPayBin())   && StringUtils.hasText(meta.getBin()))   e.setPayBin(meta.getBin());
        if (!StringUtils.hasText(e.getPayLast4()) && StringUtils.hasText(meta.getLast4())) e.setPayLast4(meta.getLast4());
        if (!StringUtils.hasText(e.getPayPg())    && StringUtils.hasText(meta.getPg()))    e.setPayPg(meta.getPg());
    }
}
