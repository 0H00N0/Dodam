package com.dodam.plan.controller;

import com.dodam.plan.Entity.PlanPaymentEntity;
import com.dodam.plan.repository.PlanPaymentRepository;
import com.dodam.plan.service.PlanPaymentGatewayService;
import com.dodam.plan.service.PlanPaymentProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pg")
@RequiredArgsConstructor
@Slf4j
public class PlanPgReturnController {

    private final PlanPaymentGatewayService pgSvc;
    private final PlanPaymentProfileService profileSvc;
    private final PlanPaymentRepository paymentRepo;

    @GetMapping("/return")
    public Map<String, Object> handleReturn(
            @RequestParam(required = false) String txId,
            @RequestParam(required = false) String paymentId,
            HttpSession session
    ) {
        String mid = (String) session.getAttribute("sid");
        if (!StringUtils.hasText(mid)) throw new IllegalStateException("NOT_LOGGED_IN");

        var lookup = pgSvc.safeLookup(txId, paymentId);

        String pg     = lookup.pg();
        String brand  = lookup.brand();
        String bin    = lookup.bin();
        String last4  = lookup.last4();
        String bKey   = lookup.billingKey();
        String raw    = lookup.rawJson();

        log.info("[/pg/return] mid={}, paymentId={}, txId={}, pg={}, brand={}, bin={}, last4={}",
                mid, lookup.paymentId(), lookup.txId(), pg, brand, bin, last4);

        PlanPaymentEntity pp = profileSvc.upsert(mid, mid, pg, brand, bin, last4);
        if (StringUtils.hasText(bKey)) pp.setPayKey(bKey);
        if (StringUtils.hasText(raw))  pp.setPayRaw(raw);
        paymentRepo.saveAndFlush(pp);

        return Map.of(
                "result", "ok",
                "mid", mid,
                "pg", pp.getPayPg(),
                "brand", pp.getPayBrand(),
                "bin", pp.getPayBin(),
                "last4", pp.getPayLast4(),
                "billingKey", pp.getPayKey()
        );
    }
}
