// src/main/java/com/dodam/plan/dto/PlanDTO.java
package com.dodam.plan.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import com.dodam.plan.Entity.PlanBenefitEntity;
import com.dodam.plan.Entity.PlanPriceEntity;
import com.dodam.plan.Entity.PlansEntity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanDTO {
    private Long planId;
    private String planCode;            // BASIC / STANDARD / ...
    private String displayName;         // 예: 베이직 (BASIC)

    // 표시용 정수 (.00 제거)
    private Long priceInt;              // 노출 가격(정수)  ← ppriceAmount 기반
    private Long rentalPriceCapInt;     // 월 대여 상한(정수) ← pbPriceCap 기반

    private String currency;            // ppriceCurr (없으면 'KRW')
    private String note;                // 혜택 설명 ← pbNote

    public static PlanDTO of(PlansEntity p,
                             PlanBenefitEntity b,
                             List<PlanPriceEntity> prices) {

        // 표시명: planName + (planCode)
        String disp = p.getPlanName().getPlanName() + " (" + p.getPlanCode() + ")";

        // 활성 가격 중 "가장 낮은 가격"을 대표 가격으로 선택 (규칙 원하면 바꾸세요: 첫번째 등)
        PlanPriceEntity chosen = null;
        if (prices != null && !prices.isEmpty()) {
            chosen = prices.stream()
                    .filter(pp -> Boolean.TRUE.equals(pp.getPpriceActive()))
                    .min(Comparator.comparing(PlanPriceEntity::getPpriceAmount)) // 최저가
                    .orElse(null);
        }

        BigDecimal amount = (chosen != null ? chosen.getPpriceAmount() : null);
        String curr = (chosen != null ? chosen.getPpriceCurr() : "KRW");

        BigDecimal cap = (b != null ? b.getPbPriceCap() : null);
        String note = (b != null ? b.getPbNote() : null);

        return PlanDTO.builder()
                .planId(p.getPlanId())
                .planCode(p.getPlanCode())
                .displayName(disp)
                .priceInt(toLong(amount))
                .rentalPriceCapInt(toLong(cap))
                .currency(curr == null ? "KRW" : curr)
                .note(note)
                .build();
    }

    private static Long toLong(BigDecimal v) {
        if (v == null) return 0L; // 0이면 프론트에서 "제한 없음" 같은 처리 용이
        return v.setScale(0, RoundingMode.DOWN).longValue(); // 소수점 버림 (.00 제거)
    }
}
