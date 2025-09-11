package com.dodam.plan.dto;

public record PlanPaymentRegisterReq(
        String billingKey,
        String pg,
        String brand,
        String bin,
        String last4,
        String raw
) {}
