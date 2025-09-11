package com.dodam.plan.service;

import com.dodam.plan.Entity.PlanPaymentEntity;
import com.dodam.plan.repository.PlanPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PlanPaymentProfileService {

    private final PlanPaymentRepository repo;

    /**
     * 결제 프로필 upsert.
     * - 존재하면 갱신, 없으면 생성
     * - 전달값이 비어있으면 기존값 유지 (메타 보전)
     */
    @Transactional
    public PlanPaymentEntity upsert(String mid, String customerId, String pg, String brand, String bin, String last4) {
        PlanPaymentEntity e = repo.findTopByMidOrderByPayIdDesc(mid)
                .orElseGet(() -> PlanPaymentEntity.builder()
                        .mid(mid)
                        .payCustomer(customerId)
                        .build());

        if (StringUtils.hasText(customerId)) e.setPayCustomer(customerId);
        if (StringUtils.hasText(pg))    e.setPayPg(pg);
        if (StringUtils.hasText(brand)) e.setPayBrand(brand);
        if (StringUtils.hasText(bin))   e.setPayBin(bin);
        if (StringUtils.hasText(last4)) e.setPayLast4(last4);

        return repo.save(e);
    }
}
