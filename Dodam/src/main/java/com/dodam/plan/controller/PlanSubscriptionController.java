package com.dodam.plan.controller;

import com.dodam.plan.service.PlanPaySubService;
import com.dodam.plan.enums.PlanEnums.PmBillingMode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 구독 시작 / 내 구독 조회 등 (세션 기반)
 */
@RestController
@RequestMapping("/sub")
@RequiredArgsConstructor
public class PlanSubscriptionController {

    private final PlanPaySubService subSvc;

    /**
     * 구독 시작 (PlanMember + 첫 Invoice 생성)
     */
    @PostMapping
    public ResponseEntity<StartRes> start(HttpSession session,
                                          @RequestBody StartReq req) {
        Long mnum = (Long) session.getAttribute("sid");
        if (mnum == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var pm = subSvc.start(
                mnum,
                req.planId(),
                req.ppriceId(),
                req.ptermId(),
                req.payId(),
                req.mode(),
                req.firstAmount()
        );

        return ResponseEntity.ok(new StartRes(pm.getPmId()));
    }

    // 요청 DTO
    public record StartReq(Long planId, Long ppriceId, Long ptermId,
                           Long payId, PmBillingMode mode, BigDecimal firstAmount) {}

    // 응답 DTO
    public record StartRes(Long pmId) {}
}
