// src/main/java/com/dodam/plan/service/dto/CardMeta.java
package com.dodam.plan.dto;

public record PlanCardMeta(
        String brand,
        String bin,
        String last4,
        String issuerName
) {
    public static PlanCardMeta empty() { return new PlanCardMeta("", "", "", ""); }
}
