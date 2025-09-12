package com.dodam.admin.dto;

import com.dodam.plan.Entity.PlanNameEntity;
import com.dodam.plan.Entity.PlansEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class AdminPlanDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long planId;
        private String planName;
        private String planCode;
        private Boolean planActive;
        private LocalDateTime planCreate;

        public static Response fromEntity(PlansEntity entity) {
            return Response.builder()
                    .planId(entity.getPlanId())
                    .planName(entity.getPlanName().getPlanName())
                    .planCode(entity.getPlanCode())
                    .planActive(entity.getPlanActive())
                    .planCreate(entity.getPlanCreate())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long planNameId;
        private String planCode;
        private Boolean planActive;

        public PlansEntity toEntity(PlanNameEntity planNameEntity) {
            return PlansEntity.builder()
                    .planName(planNameEntity)
                    .planCode(planCode)
                    .planActive(planActive)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private Long planNameId;
        private String planCode;
        private Boolean planActive;
    }
}
