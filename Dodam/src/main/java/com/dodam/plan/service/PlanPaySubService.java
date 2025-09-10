package com.dodam.plan.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import com.dodam.member.repository.MemberRepository;
import com.dodam.plan.Entity.*;
import com.dodam.plan.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.dodam.plan.enums.PlanEnums.PiStatus;
import com.dodam.plan.enums.PlanEnums.PmBillingMode;
import com.dodam.plan.enums.PlanEnums.PmStatus;

@Service @RequiredArgsConstructor
public class PlanPaySubService {
  private final PlanMemberRepository memberRepo;
  private final PlanInvoiceRepository invoiceRepo;
  private final MemberRepository memberRepo2;
  private final PlanPaymentRepository paymentRepo;
  private final PlanPriceRepository priceRepo;
  private final PlansRepository plansRepo;
  private final PlanTermsRepository termsRepo;

  @Transactional
  public PlanMember start(Long mnum, Long planId, Long ppriceId, Long ptermId, Long payId,
             PmBillingMode mode, BigDecimal firstAmount) {

    var member = memberRepo2.findById(mnum).orElseThrow();
    var plan   = plansRepo.findById(planId).orElseThrow();
    var price  = priceRepo.findById(ppriceId).orElseThrow();
    var terms  = termsRepo.findById(ptermId).orElseThrow();
    var pay    = paymentRepo.findById(payId).orElseThrow();

    var now = LocalDateTime.now();

    var pm = PlanMember.builder()
      .member(member).plan(plan).price(price).terms(terms).payment(pay)
      .pmStat(PmStatus.ACTIVE).pmBilMode(mode)
      .pmStart(now).pmTermStart(now).pmTermEnd(now.plusMonths(1)).pmNextBil(now.plusMonths(1))
      .build();
    pm = memberRepo.save(pm);

    var pi = PlanInvoiceEntity.builder()
      .planMember(pm)
      .piStart(pm.getPmTermStart())
      .piEnd(pm.getPmTermEnd())
      .piAmount(firstAmount)
      .piCurr("KRW")
      .piStat(PiStatus.PENDING)
      .piUid(generatePaymentId(pm.getPmId()))
      .build();
    invoiceRepo.save(pi);

    return pm;
  }

  private String generatePaymentId(Long pmId) {
    return "sub_" + pmId + "_" +
      java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
  }
}
