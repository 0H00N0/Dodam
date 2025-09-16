// src/main/java/com/dodam/plan/controller/PlanSubscriptionController.java
package com.dodam.plan.controller;

import com.dodam.member.entity.MemberEntity;
import com.dodam.member.repository.MemberRepository;

import com.dodam.plan.Entity.PlanInvoiceEntity;
import com.dodam.plan.Entity.PlanMember;
import com.dodam.plan.Entity.PlanPaymentEntity;
import com.dodam.plan.Entity.PlanPriceEntity;
import com.dodam.plan.Entity.PlanTermsEntity;
import com.dodam.plan.Entity.PlansEntity;

import com.dodam.plan.dto.PlanSubscriptionStartReq;
import com.dodam.plan.enums.PlanEnums.PiStatus;
import com.dodam.plan.enums.PlanEnums.PmBillingMode;
import com.dodam.plan.enums.PlanEnums.PmStatus;

import com.dodam.plan.repository.PlanInvoiceRepository;
import com.dodam.plan.repository.PlanMemberRepository;
import com.dodam.plan.repository.PlanPaymentRepository;
import com.dodam.plan.repository.PlanPriceRepository;
import com.dodam.plan.repository.PlanTermsRepository;
import com.dodam.plan.repository.PlansRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/subscriptions")
public class PlanSubscriptionController {

    private final PlanInvoiceRepository invoiceRepo;
    private final PlanMemberRepository planMemberRepo;
    private final PlansRepository plansRepo;
    private final PlanTermsRepository termsRepo;
    private final PlanPriceRepository priceRepo;
    private final PlanPaymentRepository paymentRepo;
    private final MemberRepository memberRepo;

    @PostMapping(value = "/start", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<?> start(@RequestBody PlanSubscriptionStartReq req, HttpSession session) {
        final String mid = (String) session.getAttribute("sid");
        if (!StringUtils.hasText(mid)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "LOGIN_REQUIRED"));
        }

        // 0) 기본 파라미터 정규화
        final int months = (req.getMonths() != null && req.getMonths() > 0) ? req.getMonths() : 1;
        final String planCode = (req.getPlanCode() != null) ? req.getPlanCode().trim() : null;
        if (!StringUtils.hasText(planCode)) {
            return ResponseEntity.badRequest().body(Map.of("error", "MISSING_PLAN_CODE"));
        }

        // 1) 회원/결제수단/플랜/약정/가격 조회 (실제 프로젝트 리포지토리 시그니처 그대로 사용)
        MemberEntity member = memberRepo.findByMid(mid)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다. mid=" + mid));

        // 최신 등록 결제수단 (없으면 구독 시작 불가)
        PlanPaymentEntity payment = paymentRepo.findTopByMidOrderByPayIdDesc(mid)
                .orElseThrow(() -> new IllegalStateException("결제수단이 없습니다. 먼저 카드(빌링키)를 등록하세요."));

        // 플랜
        PlansEntity plan = plansRepo.findByPlanCodeIgnoreCase(planCode)
                .orElseGet(() -> plansRepo.findByPlanCodeEqualsIgnoreCase(planCode)
                        .orElseThrow(() -> new IllegalStateException("플랜 코드가 유효하지 않습니다. planCode=" + planCode)));

        // 약정(개월) → months와 정확히 매칭
        PlanTermsEntity terms = termsRepo.findByPtermMonth(months)
                .orElseThrow(() -> new IllegalStateException("해당 개월 약정이 없습니다. months=" + months));

        // 가격: ACTIVE + AUTO 모드 사용 (리포지토리 시그니처 그대로)
        PlanPriceEntity price = priceRepo
                .findByPlan_PlanIdAndPterm_PtermIdAndPpriceBilModeAndPpriceActiveTrue(
                        plan.getPlanId(), terms.getPtermId(), "AUTO")
                .orElseThrow(() -> new IllegalStateException("가격 정보가 없습니다. plan=" + planCode + ", months=" + months));

        final BigDecimal amount = price.getPpriceAmount(); // 프로젝트 DTO가 사용하는 정확한 게터
        final String currency = StringUtils.hasText(price.getPpriceCurr()) ? price.getPpriceCurr() : "KRW";

        // 2) PlanMember 조회(없으면 생성)
        PlanMember pm = planMemberRepo.findByMember_Mid(mid).orElse(null);
        if (pm == null) {
            pm = PlanMember.builder()
                    .member(member)
                    .payment(payment)
                    .plan(plan)
                    .terms(terms)
                    .price(price)
                    .pmStat(PmStatus.ACTIVE)           // 설계상 ACTIVE로 시작 (필요 시 INIT로 변경)
                    .pmBilMode(PmBillingMode.AUTO)
                    .pmStart(LocalDateTime.now())
                    .pmTermStart(LocalDateTime.now())
                    .pmTermEnd(LocalDateTime.now().plusMonths(months))
                    .pmNextBil(LocalDateTime.now().plusMonths(months))
                    .pmCycle(months)
                    .pmCancelCheck(false)
                    .build();
            pm = planMemberRepo.save(pm);
            log.info("[subscriptions/start] PlanMember created mid={}, pmId={}", mid, pm.getPmId());
        }

        // 3) 멱등 체크: 최근 10분 내 동일 금액/통화의 PENDING 인보이스
        final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        final LocalDateTime from = now.minusMinutes(10);
        var recentOpt = invoiceRepo.findRecentPendingSameAmount(mid, PiStatus.PENDING, amount, currency, from, now);
        if (recentOpt.isPresent()) {
            var inv = recentOpt.get();
            log.info("[subscriptions/start] ALREADY_PENDING mid={}, invoiceId={}", mid, inv.getPiId());

            Map<String, Object> body = new HashMap<>();
            body.put("message", "ALREADY_PENDING");
            body.put("invoiceId", inv.getPiId());
            body.put("amount", inv.getPiAmount());
            body.put("currency", inv.getPiCurr());
            body.put("start", inv.getPiStart());
            body.put("end", inv.getPiEnd());
            return ResponseEntity.ok(body);
        }

        // 4) 신규 인보이스 생성 (금액은 Price 기준, 기간은 terms 기준)
        var inv = PlanInvoiceEntity.builder()
                .planMember(pm)
                .piStart(now)
                .piEnd(now.plusMonths(months))
                .piAmount(amount)
                .piCurr(currency)
                .piStat(PiStatus.PENDING)
                .build();
        invoiceRepo.save(inv);

        log.info("[subscriptions/start] PENDING_CREATED mid={}, invoiceId={}", mid, inv.getPiId());

        Map<String, Object> body = new HashMap<>();
        body.put("message", "PENDING_CREATED");
        body.put("invoiceId", inv.getPiId());
        body.put("amount", inv.getPiAmount());
        body.put("currency", inv.getPiCurr());
        body.put("start", inv.getPiStart());
        body.put("end", inv.getPiEnd());
        return ResponseEntity.ok(body);
    }
}
