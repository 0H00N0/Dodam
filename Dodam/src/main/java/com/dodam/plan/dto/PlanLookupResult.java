// src/main/java/com/dodam/plan/dto/PlanLookupResult.java
package com.dodam.plan.dto;

public record PlanLookupResult(
        String id,
        String status,
        String rawJson
) {}
