package com.dodam.plan.controller;

import com.dodam.plan.service.PlanPaymentProfileService;
import com.dodam.plan.repository.PlanPaymentRepository;
import com.dodam.plan.Entity.PlanPaymentEntity;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 세션 기반( sid )으로 회원 식별 후, PortOne SDK로 얻은 customerId/billingKey/카드메타를 저장/조회/삭제
 */
@RestController
@RequestMapping("/billing-keys")
@RequiredArgsConstructor
public class PlanPaymentMethodController {

    private final PlanPaymentProfileService profileSvc;
    private final PlanPaymentRepository paymentRepo;

    /**
     * 빌링키 등록/갱신
     * 프론트에서 PortOne SDK로 카드 인증 완료 후 customerId, billingKey, 카드메타(pg/brand/bin/last4) 전달
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> register(HttpSession session, @RequestBody BillingKeyReq req) {
        Long mnum = (Long) session.getAttribute("sid");
        if (mnum == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // 1) 고객 프로필 upsert (회원-mnum X PG customerId 1:1 유니크 보장)
        PlanPaymentEntity pp = profileSvc.upsert(
                mnum,
                req.customerId(),
                nullToEmpty(req.pg()),
                nullToEmpty(req.brand()),
                nullToEmpty(req.bin()),
                nullToEmpty(req.last4())
        );

        // 2) 빌링키 저장/갱신
        pp.setPayKey(req.billingKey());
        // 필요 시 활성화 플래그가 있다면: pp.setActive(true);
        paymentRepo.save(pp);

        return ResponseEntity.ok(new BillingKeyRes(pp.getPayId(), pp.getPayCustomer(), masked(pp.getPayLast4())));
    }

    /**
     * 내 빌링키 목록 보기
     */
    @GetMapping("/list")
    public ResponseEntity<List<BillingKeyItem>> list(HttpSession session) {
        Long mnum = (Long) session.getAttribute("sid");
        if (mnum == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var list = paymentRepo.findByMember_Mnum(mnum).stream()
                .map(pp -> new BillingKeyItem(
                        pp.getPayId(),
                        pp.getPayCustomer(),
                        pp.getPayPg(),
                        pp.getPayBrand(),
                        pp.getPayBin(),
                        masked(pp.getPayLast4()),
                        pp.getPayKey() != null && !pp.getPayKey().isBlank()
                ))
                .toList();

        return ResponseEntity.ok(list);
    }

    /**
     * 빌링키 삭제 (주의: PG의 customer/billingKey 삭제 호출이 필요하면 profileSvc에 위임해서 함께 처리)
     */
    @DeleteMapping("/{payId}")
    @Transactional
    public ResponseEntity<?> delete(HttpSession session, @PathVariable Long payId) {
        Long mnum = (Long) session.getAttribute("sid");
        if (mnum == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var pp = paymentRepo.findById(payId).orElse(null);
        if (pp == null || !pp.getMember().getMnum().equals(mnum)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 필요 시 PortOne에 customer/billingKey 삭제 API 호출 → profileSvc에 메서드 추가하여 처리
        // profileSvc.deleteOnGateway(pp.getPayCustomer(), pp.getPayKey());

        paymentRepo.delete(pp);
        return ResponseEntity.noContent().build();
    }

    // ===== DTOs =====

    public record BillingKeyReq(
            String customerId,
            String billingKey,
            String pg,      // 예: "toss-payments"
            String brand,   // 예: "KB", "Hyundai"
            String bin,     // 카드 BIN (앞 6)
            String last4    // 카드 뒤 4
    ) {}

    public record BillingKeyRes(
            Long payId,
            String customerId,
            String last4Masked
    ) {}

    public record BillingKeyItem(
            Long payId,
            String customerId,
            String pg,
            String brand,
            String bin,
            String last4Masked,
            boolean hasBillingKey
    ) {}

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
    private static String masked(String last4) {
        return (last4 == null || last4.isBlank()) ? "" : "****-****-****-" + last4;
    }
}
