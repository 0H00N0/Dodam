package com.dodam.product.dto.statistics;

import com.dodam.product.entity.Gifticon.GifticonStatus;
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
 * 기프티콘 통계 DTO
 * 기프티콘 관련 집계 데이터와 분석 정보를 제공하는 통계 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GifticonStatisticsDto {

    // === 기본 집계 정보 ===

    /**
     * 전체 기프티콘 수
     */
    private Long totalGifticonCount;

    /**
     * 발급된 기프티콘 수
     */
    private Long issuedCount;

    /**
     * 사용된 기프티콘 수
     */
    private Long usedCount;

    /**
     * 만료된 기프티콘 수
     */
    private Long expiredCount;

    /**
     * 양도된 기프티콘 수
     */
    private Long transferredCount;

    /**
     * 취소된 기프티콘 수
     */
    private Long cancelledCount;

    /**
     * 오늘 발급된 기프티콘 수
     */
    private Long todayIssuedCount;

    /**
     * 오늘 사용된 기프티콘 수
     */
    private Long todayUsedCount;

    // === 가치 관련 통계 ===

    /**
     * 전체 기프티콘 총 가치
     */
    private BigDecimal totalValue;

    /**
     * 사용된 기프티콘 총 가치
     */
    private BigDecimal usedValue;

    /**
     * 만료된 기프티콘 총 가치 (손실액)
     */
    private BigDecimal expiredValue;

    /**
     * 평균 기프티콘 가치
     */
    private BigDecimal averageValue;

    /**
     * 최고 가치 기프티콘
     */
    private BigDecimal maxValue;

    /**
     * 최저 가치 기프티콘
     */
    private BigDecimal minValue;

    // === 사용률 관련 지표 ===

    /**
     * 전체 사용률 (%)
     */
    private Double usageRate;

    /**
     * 만료율 (%)
     */
    private Double expirationRate;

    /**
     * 양도율 (%)
     */
    private Double transferRate;

    /**
     * 취소율 (%)
     */
    private Double cancellationRate;

    /**
     * 활성 기프티콘 비율 (발급된 상태) (%)
     */
    private Double activeRate;

    // === 상태별 분포 ===

    /**
     * 상태별 기프티콘 수 분포
     */
    private Map<GifticonStatus, Long> gifticonsByStatus;

    /**
     * 상태별 비율 (백분율)
     */
    private Map<GifticonStatus, Double> statusDistribution;

    /**
     * 상태별 가치 분포
     */
    private Map<GifticonStatus, BigDecimal> valueByStatus;

    // === 상품별 인기도 분석 ===

    /**
     * 기프티콘이 있는 상품 수
     */
    private Long productWithGifticonCount;

    /**
     * 상품별 기프티콘 수 분포
     */
    private Map<String, Long> gifticonsByProduct;

    /**
     * 인기 기프티콘 상품 TOP 10 (발급량 기준)
     */
    private List<PopularGifticonProduct> topIssuedProducts;

    /**
     * 인기 기프티콘 상품 TOP 10 (사용량 기준)
     */
    private List<PopularGifticonProduct> topUsedProducts;

    /**
     * 만료율이 높은 상품 TOP 10
     */
    private List<HighExpirationProduct> highExpirationProducts;

    // === 카테고리별 분석 ===

    /**
     * 카테고리별 기프티콘 발급 수
     */
    private Map<String, Long> gifticonsByCategory;

    /**
     * 카테고리별 사용률
     */
    private Map<String, Double> usageRateByCategory;

    /**
     * 카테고리별 평균 가치
     */
    private Map<String, BigDecimal> averageValueByCategory;

    // === 사용자 행동 분석 ===

    /**
     * 기프티콘 보유 사용자 수 (유니크)
     */
    private Long uniqueHolders;

    /**
     * 활발한 사용자 수 (월 1회 이상 사용)
     */
    private Long activeUsers;

    /**
     * 사용자당 평균 기프티콘 수
     */
    private Double averageGifticonsPerUser;

    /**
     * 사용자당 평균 사용 기프티콘 수
     */
    private Double averageUsedPerUser;

    /**
     * 가장 활발한 사용자들 TOP 10
     */
    private List<TopGifticonUser> topUsers;

    // === 시간 관련 분석 ===

    /**
     * 평균 사용까지 소요 시간 (일)
     */
    private Double averageDaysToUse;

    /**
     * 평균 만료까지 남은 시간 (일)
     */
    private Double averageDaysToExpiry;

    /**
     * 만료 임박 기프티콘 수 (7일 이내)
     */
    private Long soonToExpireCount;

    /**
     * 만료 임박 기프티콘 가치
     */
    private BigDecimal soonToExpireValue;

    // === 시계열 데이터 ===

    /**
     * 월별 발급 수 (최근 12개월)
     */
    private Map<String, Long> monthlyIssuanceCount;

    /**
     * 월별 사용 수 (최근 12개월)
     */
    private Map<String, Long> monthlyUsageCount;

    /**
     * 일별 발급/사용 수 (최근 30일)
     */
    private Map<String, Long> dailyIssuanceCount;
    
    private Map<String, Long> dailyUsageCount;

    /**
     * 분기별 성장률 (%)
     */
    private Map<String, Double> quarterlyGrowthRate;

    // === 효율성 지표 ===

    /**
     * 기프티콘 회전율 (사용률 기준)
     */
    private Double turnoverRate;

    /**
     * ROI (Return on Investment) - 사용된 가치 / 발급된 가치
     */
    private Double roi;

    /**
     * 손실률 - 만료된 가치 / 전체 가치
     */
    private Double lossRate;

    /**
     * 효율성 점수 (0-100)
     */
    private Double efficiencyScore;

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
     * 사용률 계산 및 설정
     */
    public void calculateUsageRate() {
        if (totalGifticonCount == null || totalGifticonCount == 0) {
            this.usageRate = 0.0;
            return;
        }
        this.usageRate = (usedCount != null) ? 
            (usedCount.doubleValue() / totalGifticonCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 만료율 계산 및 설정
     */
    public void calculateExpirationRate() {
        if (totalGifticonCount == null || totalGifticonCount == 0) {
            this.expirationRate = 0.0;
            return;
        }
        this.expirationRate = (expiredCount != null) ? 
            (expiredCount.doubleValue() / totalGifticonCount.doubleValue() * 100) : 0.0;
    }

    /**
     * ROI 계산
     */
    public Double calculateROI() {
        if (totalValue == null || totalValue.equals(BigDecimal.ZERO)) {
            return 0.0;
        }
        if (usedValue == null) {
            return 0.0;
        }
        return usedValue.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue();
    }

    /**
     * 손실률 계산
     */
    public Double calculateLossRate() {
        if (totalValue == null || totalValue.equals(BigDecimal.ZERO)) {
            return 0.0;
        }
        if (expiredValue == null) {
            return 0.0;
        }
        return expiredValue.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue();
    }

    /**
     * 효율성 점수 계산 (0-100)
     */
    public Double calculateEfficiencyScore() {
        double score = 0.0;
        
        // 사용률 (40점 만점)
        if (usageRate != null) {
            score += Math.min(usageRate * 0.4, 40.0);
        }
        
        // 낮은 만료율 (30점 만점)
        if (expirationRate != null) {
            score += Math.max(0, 30.0 - (expirationRate * 0.6)); // 만료율이 낮을수록 높은 점수
        }
        
        // 회전율 (20점 만점)
        if (turnoverRate != null) {
            score += Math.min(turnoverRate * 0.2, 20.0);
        }
        
        // ROI (10점 만점)
        Double roiValue = calculateROI();
        if (roiValue != null) {
            score += Math.min(roiValue * 0.1, 10.0);
        }
        
        return Math.min(score, 100.0);
    }

    /**
     * 건강한 기프티콘 생태계인지 확인
     */
    public boolean isHealthyEcosystem() {
        if (efficiencyScore == null) {
            this.efficiencyScore = calculateEfficiencyScore();
        }
        return this.efficiencyScore >= 70.0;
    }

    /**
     * 효율성 등급 반환
     */
    public String getEfficiencyGrade() {
        if (efficiencyScore == null) {
            this.efficiencyScore = calculateEfficiencyScore();
        }
        
        if (efficiencyScore >= 90) return "A+";
        if (efficiencyScore >= 80) return "A";
        if (efficiencyScore >= 70) return "B";
        if (efficiencyScore >= 60) return "C";
        return "D";
    }

    // === 내부 클래스들 ===

    /**
     * 인기 기프티콘 상품 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularGifticonProduct {
        private Long productId;
        private String productName;
        private String categoryName;
        private Long totalCount;
        private Long usedCount;
        private BigDecimal totalValue;
        private Double usageRate;
        private Integer rank;
    }

    /**
     * 만료율이 높은 상품 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighExpirationProduct {
        private Long productId;
        private String productName;
        private String categoryName;
        private Long totalCount;
        private Long expiredCount;
        private Double expirationRate;
        private BigDecimal lossValue;
    }

    /**
     * 활발한 기프티콘 사용자 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopGifticonUser {
        private Long userId;
        private String userName; // 마스킹된 이름
        private Long totalGifticonCount;
        private Long usedCount;
        private BigDecimal totalValue;
        private Double usageRate;
        private LocalDateTime lastUsedDate;
    }

    // === 정적 팩토리 메서드들 ===

    /**
     * 기본 통계 생성
     */
    public static GifticonStatisticsDto createBasicStats() {
        return GifticonStatisticsDto.builder()
            .totalGifticonCount(0L)
            .issuedCount(0L)
            .usedCount(0L)
            .expiredCount(0L)
            .transferredCount(0L)
            .cancelledCount(0L)
            .todayIssuedCount(0L)
            .todayUsedCount(0L)
            .totalValue(BigDecimal.ZERO)
            .usedValue(BigDecimal.ZERO)
            .expiredValue(BigDecimal.ZERO)
            .averageValue(BigDecimal.ZERO)
            .maxValue(BigDecimal.ZERO)
            .minValue(BigDecimal.ZERO)
            .usageRate(0.0)
            .expirationRate(0.0)
            .transferRate(0.0)
            .cancellationRate(0.0)
            .activeRate(0.0)
            .productWithGifticonCount(0L)
            .uniqueHolders(0L)
            .activeUsers(0L)
            .averageGifticonsPerUser(0.0)
            .averageUsedPerUser(0.0)
            .averageDaysToUse(0.0)
            .averageDaysToExpiry(0.0)
            .soonToExpireCount(0L)
            .soonToExpireValue(BigDecimal.ZERO)
            .turnoverRate(0.0)
            .roi(0.0)
            .lossRate(0.0)
            .efficiencyScore(0.0)
            .generatedAt(LocalDateTime.now())
            .dataAsOf(LocalDateTime.now())
            .build();
    }

    // === 유틸리티 메서드들 ===

    /**
     * 개선 권장사항 생성
     */
    public List<String> getImprovementRecommendations() {
        List<String> recommendations = new java.util.ArrayList<>();
        
        if (usageRate != null && usageRate < 50) {
            recommendations.add("기프티콘 사용률이 낮습니다. 사용 독려 캠페인을 진행해보세요.");
        }
        
        if (expirationRate != null && expirationRate > 20) {
            recommendations.add("만료율이 높습니다. 만료 임박 알림을 강화하고 유효기간을 연장해보세요.");
        }
        
        if (averageDaysToUse != null && averageDaysToUse > 30) {
            recommendations.add("사용까지 소요 시간이 깁니다. 즉시 사용 인센티브를 제공해보세요.");
        }
        
        if (soonToExpireCount != null && totalGifticonCount != null && totalGifticonCount > 0) {
            double soonExpireRate = soonToExpireCount.doubleValue() / totalGifticonCount.doubleValue() * 100;
            if (soonExpireRate > 15) {
                recommendations.add("만료 임박 기프티콘이 많습니다. 긴급 사용 촉진 이벤트를 고려해보세요.");
            }
        }
        
        Double efficiency = calculateEfficiencyScore();
        if (efficiency < 60) {
            recommendations.add("전반적인 기프티콘 효율성이 낮습니다. 운영 전략을 재검토해보세요.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("효율적으로 기프티콘 시스템이 운영되고 있습니다!");
        }
        
        return recommendations;
    }

    /**
     * 위험 요소 분석
     */
    public List<String> getRiskFactors() {
        List<String> risks = new java.util.ArrayList<>();
        
        if (soonToExpireValue != null && totalValue != null && totalValue.compareTo(BigDecimal.ZERO) > 0) {
            double riskRatio = soonToExpireValue.divide(totalValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).doubleValue();
            if (riskRatio > 10) {
                risks.add(String.format("만료 위험 금액: %.1f%% (%,d원)", riskRatio, soonToExpireValue.intValue()));
            }
        }
        
        if (expirationRate != null && expirationRate > 25) {
            risks.add(String.format("높은 만료율: %.1f%% (업계 평균 대비 높음)", expirationRate));
        }
        
        if (usageRate != null && usageRate < 40) {
            risks.add(String.format("낮은 사용률: %.1f%% (개선 필요)", usageRate));
        }
        
        if (lossRate != null && lossRate > 15) {
            risks.add(String.format("높은 손실률: %.1f%% (경영 개선 필요)", lossRate));
        }
        
        return risks;
    }

    /**
     * 성과 하이라이트 생성
     */
    public List<String> getPerformanceHighlights() {
        List<String> highlights = new java.util.ArrayList<>();
        
        if (usageRate != null && usageRate > 70) {
            highlights.add(String.format("우수한 사용률: %.1f%%", usageRate));
        }
        
        if (expirationRate != null && expirationRate < 10) {
            highlights.add(String.format("낮은 만료율: %.1f%%", expirationRate));
        }
        
        if (roi != null && roi > 80) {
            highlights.add(String.format("높은 ROI: %.1f%%", roi));
        }
        
        if (todayUsedCount != null && todayUsedCount > 0) {
            highlights.add(String.format("오늘 %,d개 기프티콘 사용", todayUsedCount));
        }
        
        if (activeUsers != null && uniqueHolders != null && uniqueHolders > 0) {
            double activeUserRate = activeUsers.doubleValue() / uniqueHolders.doubleValue() * 100;
            if (activeUserRate > 60) {
                highlights.add(String.format("활발한 사용자 비율: %.1f%%", activeUserRate));
            }
        }
        
        return highlights;
    }

    /**
     * 통계 요약 텍스트 생성
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append(String.format("총 %,d개 기프티콘 중 %,d개(%.1f%%)가 사용되었습니다. ",
            totalGifticonCount != null ? totalGifticonCount : 0,
            usedCount != null ? usedCount : 0,
            usageRate != null ? usageRate : 0.0));
        
        if (totalValue != null) {
            summary.append(String.format("전체 가치는 %,d원이며, ", totalValue.intValue()));
        }
        
        if (expirationRate != null) {
            summary.append(String.format("만료율은 %.1f%%입니다. ", expirationRate));
        }
        
        summary.append(String.format("기프티콘 효율성은 %.0f점으로 %s 등급입니다.",
            efficiencyScore != null ? efficiencyScore : calculateEfficiencyScore(),
            getEfficiencyGrade()));
        
        return summary.toString();
    }

    /**
     * 모든 비율 필드 계산
     */
    public void calculateAllRates() {
        calculateUsageRate();
        calculateExpirationRate();
        
        if (totalGifticonCount != null && totalGifticonCount > 0) {
            this.transferRate = (transferredCount != null) ? 
                (transferredCount.doubleValue() / totalGifticonCount.doubleValue() * 100) : 0.0;
            
            this.cancellationRate = (cancelledCount != null) ? 
                (cancelledCount.doubleValue() / totalGifticonCount.doubleValue() * 100) : 0.0;
            
            this.activeRate = (issuedCount != null) ? 
                (issuedCount.doubleValue() / totalGifticonCount.doubleValue() * 100) : 0.0;
        }
        
        this.roi = calculateROI();
        this.lossRate = calculateLossRate();
        this.efficiencyScore = calculateEfficiencyScore();
    }
}