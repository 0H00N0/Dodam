package com.dodam.product.dto.statistics;

import com.dodam.product.entity.EventReward.RewardStatus;
import com.dodam.product.entity.EventReward.RewardType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 이벤트 통계 DTO
 * 이벤트 관련 집계 데이터와 성과 분석 정보를 제공하는 통계 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventStatisticsDto {

    // === 기본 집계 정보 ===

    /**
     * 전체 이벤트 수
     */
    private Long totalEventCount;

    /**
     * 진행 중인 이벤트 수
     */
    private Long activeEventCount;

    /**
     * 완료된 이벤트 수
     */
    private Long completedEventCount;

    /**
     * 예정된 이벤트 수
     */
    private Long scheduledEventCount;

    /**
     * 취소된 이벤트 수
     */
    private Long cancelledEventCount;

    /**
     * 이번 달 신규 이벤트 수
     */
    private Long monthlyNewEventCount;

    // === 참여 관련 통계 ===

    /**
     * 전체 이벤트 참여자 수 (유니크)
     */
    private Long totalParticipants;

    /**
     * 총 이벤트 참여 건수
     */
    private Long totalParticipations;

    /**
     * 활발한 참여자 수 (월 2회 이상 참여)
     */
    private Long activeParticipants;

    /**
     * 평균 이벤트별 참여자 수
     */
    private Double averageParticipantsPerEvent;

    /**
     * 사용자당 평균 참여 이벤트 수
     */
    private Double averageEventsPerUser;

    /**
     * 전체 참여율 (%)
     */
    private Double overallParticipationRate;

    // === 보상 관련 통계 ===

    /**
     * 전체 보상 건수
     */
    private Long totalRewardCount;

    /**
     * 지급 대기 중인 보상 수
     */
    private Long pendingRewardCount;

    /**
     * 수령 가능한 보상 수
     */
    private Long eligibleRewardCount;

    /**
     * 지급된 보상 수
     */
    private Long rewardedCount;

    /**
     * 만료된 보상 수
     */
    private Long expiredRewardCount;

    /**
     * 취소된 보상 수
     */
    private Long cancelledRewardCount;

    // === 보상 가치 분석 ===

    /**
     * 전체 보상 가치
     */
    private BigDecimal totalRewardValue;

    /**
     * 지급된 보상 가치
     */
    private BigDecimal rewardedValue;

    /**
     * 만료된 보상 가치 (손실액)
     */
    private BigDecimal expiredRewardValue;

    /**
     * 평균 보상 가치
     */
    private BigDecimal averageRewardValue;

    /**
     * 최고 가치 보상
     */
    private BigDecimal maxRewardValue;

    /**
     * 최저 가치 보상
     */
    private BigDecimal minRewardValue;

    // === 성과 지표 ===

    /**
     * 보상 지급률 (%)
     */
    private Double rewardDeliveryRate;

    /**
     * 보상 수령률 (%)
     */
    private Double rewardClaimRate;

    /**
     * 보상 만료율 (%)
     */
    private Double rewardExpirationRate;

    /**
     * 이벤트 완료율 (%)
     */
    private Double eventCompletionRate;

    /**
     * ROI (Return on Investment) - 효과 대비 비용
     */
    private Double eventROI;

    // === 보상 유형별 분석 ===

    /**
     * 보상 유형별 지급 건수
     */
    private Map<RewardType, Long> rewardsByType;

    /**
     * 보상 유형별 총 가치
     */
    private Map<RewardType, BigDecimal> valueByRewardType;

    /**
     * 보상 유형별 인기도 (수령률 기준)
     */
    private Map<RewardType, Double> popularityByRewardType;

    // === 이벤트 상태별 분포 ===

    /**
     * 보상 상태별 분포
     */
    private Map<RewardStatus, Long> rewardsByStatus;

    /**
     * 상태별 비율 (백분율)
     */
    private Map<RewardStatus, Double> statusDistribution;

    // === 인기 이벤트 분석 ===

    /**
     * 참여자가 많은 이벤트 TOP 10
     */
    private List<PopularEvent> mostParticipatedEvents;

    /**
     * 성과가 좋은 이벤트 TOP 10 (완료율 기준)
     */
    private List<TopPerformingEvent> topPerformingEvents;

    /**
     * 보상 가치가 높은 이벤트 TOP 10
     */
    private List<HighValueEvent> highValueEvents;

    // === 카테고리별 분석 ===

    /**
     * 카테고리별 이벤트 수
     */
    private Map<String, Long> eventsByCategory;

    /**
     * 카테고리별 평균 참여자 수
     */
    private Map<String, Double> averageParticipantsByCategory;

    /**
     * 카테고리별 성공률
     */
    private Map<String, Double> successRateByCategory;

    // === 사용자 행동 분석 ===

    /**
     * 신규 참여자 수 (이번 달)
     */
    private Long newParticipantsThisMonth;

    /**
     * 재참여자 수 (이전에 참여한 적 있는 사용자)
     */
    private Long returningParticipants;

    /**
     * 충성도 높은 참여자 수 (5회 이상 참여)
     */
    private Long loyalParticipants;

    /**
     * 가장 활발한 참여자들 TOP 10
     */
    private List<TopEventParticipant> topParticipants;

    // === 시계열 데이터 ===

    /**
     * 월별 이벤트 수 (최근 12개월)
     */
    private Map<String, Long> monthlyEventCount;

    /**
     * 월별 참여자 수 (최근 12개월)
     */
    private Map<String, Long> monthlyParticipantCount;

    /**
     * 일별 참여자 수 (최근 30일)
     */
    private Map<String, Long> dailyParticipantCount;

    /**
     * 분기별 성장률 (%)
     */
    private Map<String, Double> quarterlyGrowthRate;

    // === 효과성 분석 ===

    /**
     * 이벤트 효과성 점수 (0-100)
     */
    private Double effectivenessScore;

    /**
     * 참여 만족도 점수 (추정값)
     */
    private Double satisfactionScore;

    /**
     * 이벤트 다양성 지수
     */
    private Integer diversityIndex;

    /**
     * 운영 효율성 점수
     */
    private Double operationalEfficiency;

    // === 메타데이터 ===

    /**
     * 통계 생성 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime generatedAt;

    /**
     * 데이터 기준 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime dataAsOf;

    /**
     * 통계 생성에 소요된 시간 (밀리초)
     */
    private Long processingTimeMs;

    // === 계산된 필드들 ===

    /**
     * 활성 이벤트 비율 (%)
     */
    public Double getActiveEventRate() {
        if (totalEventCount == null || totalEventCount == 0) {
            return 0.0;
        }
        return (activeEventCount != null) ? 
            (activeEventCount.doubleValue() / totalEventCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 보상 지급률 계산
     */
    public Double calculateRewardDeliveryRate() {
        if (totalRewardCount == null || totalRewardCount == 0) {
            return 0.0;
        }
        return (rewardedCount != null) ? 
            (rewardedCount.doubleValue() / totalRewardCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 보상 만료율 계산
     */
    public Double calculateRewardExpirationRate() {
        if (totalRewardCount == null || totalRewardCount == 0) {
            return 0.0;
        }
        return (expiredRewardCount != null) ? 
            (expiredRewardCount.doubleValue() / totalRewardCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 이벤트 ROI 계산
     */
    public Double calculateEventROI() {
        if (totalRewardValue == null || totalRewardValue.equals(BigDecimal.ZERO)) {
            return 0.0;
        }
        if (rewardedValue == null) {
            return 0.0;
        }
        
        // ROI = (이벤트 효과 - 투입 비용) / 투입 비용 * 100
        // 여기서는 단순화하여 지급된 보상 비율로 계산
        return rewardedValue.divide(totalRewardValue, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100")).doubleValue();
    }

    /**
     * 효과성 점수 계산 (0-100)
     */
    public Double calculateEffectivenessScore() {
        double score = 0.0;
        
        // 참여율 (25점 만점)
        if (overallParticipationRate != null) {
            score += Math.min(overallParticipationRate * 0.25, 25.0);
        }
        
        // 보상 지급률 (20점 만점)
        Double deliveryRate = calculateRewardDeliveryRate();
        if (deliveryRate != null) {
            score += Math.min(deliveryRate * 0.2, 20.0);
        }
        
        // 이벤트 완료율 (20점 만점)
        if (eventCompletionRate != null) {
            score += Math.min(eventCompletionRate * 0.2, 20.0);
        }
        
        // 낮은 만료율 (15점 만점)
        Double expirationRate = calculateRewardExpirationRate();
        if (expirationRate != null) {
            score += Math.max(0, 15.0 - (expirationRate * 0.3)); // 만료율이 낮을수록 높은 점수
        }
        
        // 참여자 충성도 (10점 만점)
        if (totalParticipants != null && totalParticipants > 0 && loyalParticipants != null) {
            double loyaltyRate = loyalParticipants.doubleValue() / totalParticipants.doubleValue() * 100;
            score += Math.min(loyaltyRate * 0.1, 10.0);
        }
        
        // 다양성 지수 (10점 만점)
        if (diversityIndex != null) {
            score += diversityIndex * 0.1;
        }
        
        return Math.min(score, 100.0);
    }

    /**
     * 건강한 이벤트 생태계인지 확인
     */
    public boolean isHealthyEventEcosystem() {
        if (effectivenessScore == null) {
            this.effectivenessScore = calculateEffectivenessScore();
        }
        return this.effectivenessScore >= 70.0;
    }

    /**
     * 효과성 등급 반환
     */
    public String getEffectivenessGrade() {
        if (effectivenessScore == null) {
            this.effectivenessScore = calculateEffectivenessScore();
        }
        
        if (effectivenessScore >= 90) return "A+";
        if (effectivenessScore >= 80) return "A";
        if (effectivenessScore >= 70) return "B";
        if (effectivenessScore >= 60) return "C";
        return "D";
    }

    // === 내부 클래스들 ===

    /**
     * 인기 이벤트 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularEvent {
        private Long eventId;
        private String eventTitle;
        private String eventDescription;
        private String category;
        private Long participantCount;
        private BigDecimal totalRewardValue;
        private Double participationRate;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer rank;
    }

    /**
     * 성과가 좋은 이벤트 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformingEvent {
        private Long eventId;
        private String eventTitle;
        private String category;
        private Long participantCount;
        private Long completedCount;
        private Double completionRate;
        private BigDecimal rewardedValue;
        private Double roi;
        private Double effectivenessScore;
    }

    /**
     * 고가치 이벤트 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighValueEvent {
        private Long eventId;
        private String eventTitle;
        private String category;
        private BigDecimal totalRewardValue;
        private BigDecimal averageRewardValue;
        private Long participantCount;
        private Double completionRate;
        private RewardType primaryRewardType;
    }

    /**
     * 활발한 이벤트 참여자 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopEventParticipant {
        private Long userId;
        private String participantName; // 마스킹된 이름
        private Long totalParticipations;
        private Long completedEvents;
        private BigDecimal totalRewardValue;
        private Double completionRate;
        private LocalDateTime lastParticipationDate;
        private String favoriteCategory;
    }

    // === 정적 팩토리 메서드들 ===

    /**
     * 기본 통계 생성
     */
    public static EventStatisticsDto createBasicStats() {
        return EventStatisticsDto.builder()
            .totalEventCount(0L)
            .activeEventCount(0L)
            .completedEventCount(0L)
            .scheduledEventCount(0L)
            .cancelledEventCount(0L)
            .monthlyNewEventCount(0L)
            .totalParticipants(0L)
            .totalParticipations(0L)
            .activeParticipants(0L)
            .averageParticipantsPerEvent(0.0)
            .averageEventsPerUser(0.0)
            .overallParticipationRate(0.0)
            .totalRewardCount(0L)
            .pendingRewardCount(0L)
            .eligibleRewardCount(0L)
            .rewardedCount(0L)
            .expiredRewardCount(0L)
            .cancelledRewardCount(0L)
            .totalRewardValue(BigDecimal.ZERO)
            .rewardedValue(BigDecimal.ZERO)
            .expiredRewardValue(BigDecimal.ZERO)
            .averageRewardValue(BigDecimal.ZERO)
            .maxRewardValue(BigDecimal.ZERO)
            .minRewardValue(BigDecimal.ZERO)
            .rewardDeliveryRate(0.0)
            .rewardClaimRate(0.0)
            .rewardExpirationRate(0.0)
            .eventCompletionRate(0.0)
            .eventROI(0.0)
            .newParticipantsThisMonth(0L)
            .returningParticipants(0L)
            .loyalParticipants(0L)
            .effectivenessScore(0.0)
            .satisfactionScore(0.0)
            .diversityIndex(0)
            .operationalEfficiency(0.0)
            .generatedAt(LocalDateTime.now())
            .dataAsOf(LocalDateTime.now())
            .build();
    }

    // === 유틸리티 메서드들 ===

    /**
     * 모든 비율 필드 계산
     */
    public void calculateAllRates() {
        this.rewardDeliveryRate = calculateRewardDeliveryRate();
        this.rewardExpirationRate = calculateRewardExpirationRate();
        this.eventROI = calculateEventROI();
        this.effectivenessScore = calculateEffectivenessScore();
        
        if (totalRewardCount != null && totalRewardCount > 0) {
            this.rewardClaimRate = (eligibleRewardCount != null) ? 
                (eligibleRewardCount.doubleValue() / totalRewardCount.doubleValue() * 100) : 0.0;
        }
        
        if (totalEventCount != null && totalEventCount > 0) {
            this.eventCompletionRate = (completedEventCount != null) ? 
                (completedEventCount.doubleValue() / totalEventCount.doubleValue() * 100) : 0.0;
        }
    }

    /**
     * 개선 권장사항 생성
     */
    public List<String> getImprovementRecommendations() {
        List<String> recommendations = new java.util.ArrayList<>();
        
        if (overallParticipationRate != null && overallParticipationRate < 30) {
            recommendations.add("전체 참여율이 낮습니다. 이벤트 홍보를 강화하고 접근성을 개선해보세요.");
        }
        
        Double deliveryRate = calculateRewardDeliveryRate();
        if (deliveryRate != null && deliveryRate < 70) {
            recommendations.add("보상 지급률이 낮습니다. 보상 조건을 검토하고 단순화해보세요.");
        }
        
        Double expirationRate = calculateRewardExpirationRate();
        if (expirationRate != null && expirationRate > 20) {
            recommendations.add("보상 만료율이 높습니다. 유효기간을 늘리고 알림을 강화해보세요.");
        }
        
        if (eventCompletionRate != null && eventCompletionRate < 80) {
            recommendations.add("이벤트 완료율이 낮습니다. 이벤트 설계와 운영 방식을 재검토해보세요.");
        }
        
        if (returningParticipants != null && totalParticipants != null && totalParticipants > 0) {
            double retentionRate = returningParticipants.doubleValue() / totalParticipants.doubleValue() * 100;
            if (retentionRate < 40) {
                recommendations.add("재참여율이 낮습니다. 지속적인 관심을 유도할 방안을 마련해보세요.");
            }
        }
        
        if (diversityIndex != null && diversityIndex < 50) {
            recommendations.add("이벤트 다양성이 부족합니다. 다양한 유형의 이벤트를 기획해보세요.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("전반적으로 효과적인 이벤트 운영이 이루어지고 있습니다!");
        }
        
        return recommendations;
    }

    /**
     * 성과 하이라이트 생성
     */
    public List<String> getPerformanceHighlights() {
        List<String> highlights = new java.util.ArrayList<>();
        
        if (overallParticipationRate != null && overallParticipationRate > 60) {
            highlights.add(String.format("높은 참여율: %.1f%%", overallParticipationRate));
        }
        
        Double deliveryRate = calculateRewardDeliveryRate();
        if (deliveryRate != null && deliveryRate > 80) {
            highlights.add(String.format("우수한 보상 지급률: %.1f%%", deliveryRate));
        }
        
        if (eventCompletionRate != null && eventCompletionRate > 85) {
            highlights.add(String.format("높은 이벤트 완료율: %.1f%%", eventCompletionRate));
        }
        
        if (loyalParticipants != null && totalParticipants != null && totalParticipants > 0) {
            double loyaltyRate = loyalParticipants.doubleValue() / totalParticipants.doubleValue() * 100;
            if (loyaltyRate > 30) {
                highlights.add(String.format("높은 참여자 충성도: %.1f%%", loyaltyRate));
            }
        }
        
        if (monthlyNewEventCount != null && monthlyNewEventCount > 0) {
            highlights.add(String.format("이번 달 신규 이벤트 %,d개", monthlyNewEventCount));
        }
        
        if (eventROI != null && eventROI > 80) {
            highlights.add(String.format("높은 이벤트 ROI: %.1f%%", eventROI));
        }
        
        return highlights;
    }

    /**
     * 이벤트 트렌드 분석
     */
    public String getEventTrend() {
        if (quarterlyGrowthRate == null || quarterlyGrowthRate.isEmpty()) {
            return "트렌드 데이터 부족";
        }
        
        double averageGrowth = quarterlyGrowthRate.values().stream()
            .mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        if (averageGrowth > 10) return "빠른 성장";
        if (averageGrowth > 5) return "꾸준한 성장";
        if (averageGrowth > 0) return "완만한 성장";
        if (averageGrowth > -5) return "정체";
        return "감소 추세";
    }

    /**
     * 통계 요약 텍스트 생성
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append(String.format("총 %,d개 이벤트에 %,d명이 참여했습니다. ",
            totalEventCount != null ? totalEventCount : 0,
            totalParticipants != null ? totalParticipants : 0));
        
        if (overallParticipationRate != null) {
            summary.append(String.format("전체 참여율은 %.1f%%이며, ", overallParticipationRate));
        }
        
        Double deliveryRate = calculateRewardDeliveryRate();
        if (deliveryRate != null) {
            summary.append(String.format("보상 지급률은 %.1f%%입니다. ", deliveryRate));
        }
        
        summary.append(String.format("이벤트 효과성은 %.0f점으로 %s 등급입니다.",
            effectivenessScore != null ? effectivenessScore : calculateEffectivenessScore(),
            getEffectivenessGrade()));
        
        return summary.toString();
    }

    /**
     * 이벤트 건강도 진단
     */
    public String diagnoseEventHealth() {
        double health = calculateEffectivenessScore();
        String trend = getEventTrend();
        
        if (health >= 90) {
            return String.format("최우수 - 이벤트 운영이 매우 효과적입니다. (%s)", trend);
        } else if (health >= 80) {
            return String.format("우수 - 전반적으로 좋은 성과를 보이고 있습니다. (%s)", trend);
        } else if (health >= 70) {
            return String.format("양호 - 적절한 수준의 이벤트 운영입니다. (%s)", trend);
        } else if (health >= 60) {
            return String.format("보통 - 일부 개선이 필요합니다. (%s)", trend);
        } else {
            return String.format("개선 필요 - 이벤트 전략을 재검토해야 합니다. (%s)", trend);
        }
    }
}