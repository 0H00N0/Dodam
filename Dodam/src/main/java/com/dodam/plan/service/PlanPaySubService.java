package com.dodam.plan.service;

import com.dodam.member.entity.MemberEntity;
import com.dodam.member.repository.MemberRepository;
import com.dodam.plan.Entity.*;
import com.dodam.plan.enums.PlanEnums.PiStatus;
import com.dodam.plan.enums.PlanEnums.PmBillingMode;
import com.dodam.plan.enums.PlanEnums.PmStatus;
import com.dodam.plan.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanPaySubService {

  private final PlansRepository plansRepo;
  private final PlanTermsRepository termsRepo;
  private final PlanPriceRepository priceRepo;
  private final PlanPaymentRepository payRepo;
  private final PlanMemberRepository pmRepo;
  private final PlanInvoiceRepository invoiceRepo;
  private final MemberRepository memberRepo;

  public record StartResult(Long pmId, Long invoiceId) {}

  @Transactional
  public StartResult startByCodeAndMonths(String mid, String planCode, int months, PmBillingMode bilMode) {
    MemberEntity member = memberRepo.findByMid(mid)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED"));

    pmRepo.findFirstByMember_MnumAndPmStat(member.getMnum(), PmStatus.ACTIVE)
        .ifPresent(it -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "ALREADY_ACTIVE"); });

    PlanPaymentEntity payment = payRepo.findTopByMidOrderByPayIdDesc(mid)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "NO_PAYMENT_METHOD"));

    PlansEntity plan = plansRepo.findByPlanCodeIgnoreCase(planCode)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "PLAN_NOT_FOUND"));

    PlanTermsEntity terms = termsRepo.findByPtermMonth(months)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "TERM_NOT_FOUND"));

    String bil = bilMode.name(); // MONTHLY / PREPAID_TERM
    PlanPriceEntity price = priceRepo
        .findByPlan_PlanIdAndPterm_PtermIdAndPpriceBilModeAndPpriceActiveTrue(
            plan.getPlanId(), terms.getPtermId(), bil)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "PRICE_NOT_FOUND"));

    // 구독 생성
    PlanMember pm = PlanMember.builder()
        .plan(plan)
        .terms(terms)
        .price(price)
        .payment(payment)
        .member(member)
        .pmStat(PmStatus.ACTIVE)
        .pmBilMode(bilMode) // ← 엔티티 필드명 정확히 일치해야 함
        .pmStart(LocalDateTime.now())
        .pmTermStart(LocalDateTime.now())
        .pmTermEnd(LocalDateTime.now().plusMonths(months))
        .pmNextBil(bilMode == PmBillingMode.MONTHLY ? LocalDateTime.now().plusMonths(1) : null)
        .pmCycle(bilMode == PmBillingMode.MONTHLY ? 1 : months)
        .pmCancelCheck(false)
        .build();
    pmRepo.save(pm);

    // 첫 인보이스
    PlanInvoiceEntity inv = PlanInvoiceEntity.builder()
        .planMember(pm)
        .piStart(pm.getPmTermStart())
        .piEnd(pm.getPmTermEnd())
        .piAmount(price.getPpriceAmount() != null ? price.getPpriceAmount() : BigDecimal.ZERO)
        .piCurr("KRW")
        .piStat(PiStatus.PENDING)
        .piUid(genPaymentId(pm.getPmId()))
        .build();
    invoiceRepo.save(inv);

    log.info("SUBSCRIBE created pmId={}, invoiceId={}, amount={}", pm.getPmId(), inv.getPiId(), inv.getPiAmount());

    return new StartResult(pm.getPmId(), inv.getPiId());
  }

  private String genPaymentId(Long pmId) {
    return "sub_" + pmId + "_" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
  }
}
