package com.dodam.product.dto.statistics;

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
 * 리뷰 좋아요 통계 DTO
 * 리뷰 좋아요 관련 집계 데이터와 분석 정보를 제공하는 통계 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewLikeStatisticsDto {

    // === 기본 집계 정보 ===

    /**
     * 전체 좋아요 수
     */
    private Long totalLikeCount;

    /**
     * 활성 좋아요 수 (is_liked = true)
     */
    private Long activeLikeCount;

    /**
     * 취소된 좋아요 수 (is_liked = false)
     */
    private Long cancelledLikeCount;

    /**
     * 오늘 새로 받은 좋아요 수
     */
    private Long todayLikeCount;

    /**
     * 이번 달 새로 받은 좋아요 수
     */
    private Long monthlyLikeCount;

    /**
     * 좋아요를 받은 리뷰 수 (유니크)
     */
    private Long likedReviewCount;

    /**
     * 좋아요를 누른 회원 수 (유니크)
     */
    private Long uniqueLikerCount;

    // === 참여 관련 통계 ===

    /**
     * 활발한 좋아요 사용자 수 (5개 이상 좋아요)
     */
    private Long activeLikerCount;

    /**
     * 회원당 평균 좋아요 수
     */
    private Double averageLikesPerMember;

    /**
     * 리뷰당 평균 좋아요 수
     */
    private Double averageLikesPerReview;

    /**
     * 좋아요 취소율 (%)
     */
    private Double cancellationRate;

    /**
     * 좋아요 유지율 (%)
     */
    private Double retentionRate;

    // === 인기도 분석 ===

    /**
     * 가장 많은 좋아요를 받은 리뷰 TOP 10
     */
    private List<MostLikedReview> topLikedReviews;

    /**
     * 가장 활발한 좋아요 사용자 TOP 10
     */
    private List<TopLiker> topLikers;

    /**
     * 좋아요를 많이 받는 상품 TOP 10
     */
    private List<PopularProductByLikes> popularProductsByLikes;

    // === 상품별 분석 ===

    /**
     * 상품별 총 좋아요 수
     */
    private Map<String, Long> likesByProduct;

    /**
     * 상품별 평균 좋아요 수
     */
    private Map<String, Double> averageLikesByProduct;

    /**
     * 좋아요를 받은 상품 수
     */
    private Long productsWithLikes;

    // === 카테고리별 분석 ===

    /**
     * 카테고리별 총 좋아요 수
     */
    private Map<String, Long> likesByCategory;

    /**
     * 카테고리별 평균 좋아요 수
     */
    private Map<String, Double> averageLikesByCategory;

    /**
     * 카테고리별 좋아요 참여율
     */
    private Map<String, Double> participationRateByCategory;

    // === 시간대별 분석 ===

    /**
     * 시간대별 좋아요 활동 분포 (0-23시)
     */
    private Map<Integer, Long> likesByHour;

    /**
     * 요일별 좋아요 활동 분포
     */
    private Map<String, Long> likesByDayOfWeek;

    /**
     * 월별 좋아요 수 (최근 12개월)
     */
    private Map<String, Long> monthlyLikeCounts;

    /**
     * 일별 좋아요 수 (최근 30일)
     */
    private Map<String, Long> dailyLikeCounts;

    /**
     * 분기별 성장률 (%)
     */
    private Map<String, Double> quarterlyGrowthRate;

    // === 사용자 행동 분석 ===

    /**
     * 신규 좋아요 사용자 수 (이번 달)
     */
    private Long newLikersThisMonth;

    /**
     * 재방문 좋아요 사용자 수 (이전에 좋아요 누른 적 있는 사용자)
     */
    private Long returningLikers;

    /**
     * 충성도 높은 좋아요 사용자 수 (월 10개 이상 좋아요)
     */
    private Long loyalLikers;

    /**
     * 평균 좋아요 간격 (일)
     */
    private Double averageDaysBetweenLikes;

    // === 품질 지표 ===

    /**
     * 안정적인 좋아요 수 (변경되지 않은 좋아요)
     */
    private Long stableLikeCount;

    /**
     * 좋아요 안정성 점수 (0-100)
     */
    private Double stabilityScore;

    /**
     * 토글 비율 (좋아요를 변경한 비율) (%)
     */
    private Double toggleRate;

    /**
     * 좋아요 품질 지수
     */
    private String qualityIndex;

    // === 상호작용 효과성 ===

    /**
     * 좋아요 영향력 점수 (가중치 기반)
     */
    private Double influenceScore;

    /**
     * 커뮤니티 참여도 점수
     */
    private Double engagementScore;

    /**
     * 좋아요 생태계 건전성 점수
     */
    private Double ecosystemHealth;

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
     * 활성 좋아요 비율 (%)
     */
    public Double getActiveLikeRate() {
        if (totalLikeCount == null || totalLikeCount == 0) {
            return 0.0;
        }
        return (activeLikeCount != null) ? 
            (activeLikeCount.doubleValue() / totalLikeCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 좋아요 취소율 계산
     */
    public void calculateCancellationRate() {
        if (totalLikeCount == null || totalLikeCount == 0) {
            this.cancellationRate = 0.0;
            return;
        }
        this.cancellationRate = (cancelledLikeCount != null) ? 
            (cancelledLikeCount.doubleValue() / totalLikeCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 좋아요 유지율 계산
     */
    public void calculateRetentionRate() {
        if (totalLikeCount == null || totalLikeCount == 0) {
            this.retentionRate = 0.0;
            return;
        }
        this.retentionRate = (activeLikeCount != null) ? 
            (activeLikeCount.doubleValue() / totalLikeCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 토글 비율 계산
     */
    public void calculateToggleRate() {
        if (totalLikeCount == null || totalLikeCount == 0) {
            this.toggleRate = 0.0;
            return;
        }
        // 실제로는 업데이트된 좋아요 수를 기반으로 계산해야 함
        this.toggleRate = (cancelledLikeCount != null) ? 
            (cancelledLikeCount.doubleValue() / totalLikeCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 안정성 점수 계산 (0-100)
     */
    public Double calculateStabilityScore() {
        if (totalLikeCount == null || totalLikeCount == 0) {
            return 0.0;
        }
        
        double score = 0.0;
        
        // 유지율 (40점 만점)
        if (retentionRate != null) {
            score += retentionRate * 0.4;
        }
        
        // 낮은 취소율 (30점 만점)
        if (cancellationRate != null) {
            score += Math.max(0, 30.0 - cancellationRate * 0.6);
        }
        
        // 안정적인 좋아요 비율 (30점 만점)
        if (stableLikeCount != null && totalLikeCount > 0) {
            double stableRate = stableLikeCount.doubleValue() / totalLikeCount.doubleValue() * 100;
            score += stableRate * 0.3;
        }
        
        return Math.min(score, 100.0);
    }

    /**
     * 참여도 점수 계산 (0-100)
     */
    public Double calculateEngagementScore() {
        double score = 0.0;
        
        // 활성 사용자 비율 (25점 만점)
        if (uniqueLikerCount != null && uniqueLikerCount > 0 && activeLikerCount != null) {
            double activeRate = activeLikerCount.doubleValue() / uniqueLikerCount.doubleValue() * 100;
            score += Math.min(activeRate * 0.25, 25.0);
        }
        
        // 리뷰 커버리지 (25점 만점)
        if (likedReviewCount != null && likedReviewCount > 0) {
            // 전체 리뷰 수 대비 좋아요 받은 리뷰 비율 (외부에서 설정되어야 함)
            score += Math.min(likedReviewCount.doubleValue() * 0.1, 25.0);
        }
        
        // 평균 좋아요 수 (25점 만점)
        if (averageLikesPerReview != null) {
            score += Math.min(averageLikesPerReview * 5, 25.0);
        }
        
        // 신규 사용자 비율 (25점 만점)
        if (uniqueLikerCount != null && uniqueLikerCount > 0 && newLikersThisMonth != null) {
            double newUserRate = newLikersThisMonth.doubleValue() / uniqueLikerCount.doubleValue() * 100;
            score += Math.min(newUserRate * 0.25, 25.0);
        }
        
        return Math.min(score, 100.0);
    }

    /**
     * 생태계 건전성 점수 계산
     */
    public Double calculateEcosystemHealth() {
        Double stability = calculateStabilityScore();
        Double engagement = calculateEngagementScore();
        
        if (stability == null || engagement == null) {
            return 0.0;
        }
        
        // 안정성 60%, 참여도 40% 가중평균
        return stability * 0.6 + engagement * 0.4;
    }

    /**
     * 건강한 좋아요 생태계인지 확인
     */
    public boolean isHealthyEcosystem() {
        Double health = calculateEcosystemHealth();
        return health != null && health >= 70.0;
    }

    /**
     * 좋아요 생태계 등급
     */
    public String getEcosystemGrade() {
        Double health = calculateEcosystemHealth();
        if (health == null) return "N/A";
        
        if (health >= 90) return "A+";
        if (health >= 80) return "A";
        if (health >= 70) return "B";
        if (health >= 60) return "C";
        return "D";
    }

    // === 내부 클래스들 ===

    /**
     * 전체 통계 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallStats {
        private Long totalLikeCount;
        private Long activeLikeCount;
        private Long cancelledLikeCount;
        private Long totalActiveLikes;  // 추가: 서비스에서 사용하는 필드
        private Long totalCancelledLikes;  // 추가: 서비스에서 사용하는 필드
        private Long totalLikes;  // 추가: 서비스에서 사용하는 총 좋아요 필드
        private Long uniqueLikerCount;
        private Double averageLikesPerReview;
        private Double retentionRate;
        private Double cancellationRate;
        private Double conversionRate;  // 추가: 서비스에서 사용하는 전환율 필드
        private Double engagementScore;
    }

    /**
     * 리뷰 좋아요 통계 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewLikeStats {
        private Long reviewId;
        private String reviewTitle;
        private Long likeCount;
        private Double likeRate;
        private LocalDateTime lastLikedAt;
    }

    /**
     * 회원 좋아요 통계 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberLikeStats {
        private Long memberId;
        private String memberName; // 마스킹된 이름
        private Long totalLikeCount;
        private Long totalLikes;  // 추가: 서비스에서 사용하는 필드
        private Long activeLikeCount;
        private Double retentionRate;
        private LocalDateTime lastLikeDate;
    }

    /**
     * 일별 좋아요 통계 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyLikeStats {
        private LocalDateTime date;
        private Long likeCount;
        private Long cancelCount;
        private Long netLikeCount;
        private Double retentionRate;
    }

    /**
     * 전환 통계 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversionStats {
        private Long totalLikes;
        private Long totalCancels;  // 추가: 서비스에서 사용하는 필드
        private Long convertedLikes;
        private Double conversionRate;
        private Long averageTimeToConvert;
    }

    /**
     * 토글 통계 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToggleStats {
        private Long reviewId;
        private String reviewTitle;
        private Long toggleCount;
        private Double finalRetentionRate;
        private LocalDateTime lastToggledAt;
    }

    /**
     * 활성 회원 통계 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveMemberStats {
        private Long memberId;
        private String memberName; // 마스킹된 이름
        private Long likeCount;
        private Long recentLikeCount;  // 추가: 서비스에서 사용하는 필드
        private Long activeCount;
        private LocalDateTime since;  // 추가: 서비스에서 사용하는 필드
        private Double activityRate;
        private LocalDateTime lastActiveDate;
    }

    /**
     * 좋아요 패턴 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikePattern {
        private Long memberId;
        private Long totalLikes;  // 추가: 서비스에서 사용하는 필드
        private Long cancelCount;  // 추가: 서비스에서 사용하는 필드
        private Double toggleRatio;  // 추가: 서비스에서 사용하는 토글 비율 필드
        private String preferredTimeSlot;
        private String preferredDayOfWeek;
        private Double averageLikesPerDay;
        private String activityPattern;
        private Map<String, Long> categoryPreferences;
    }

    /**
     * 가장 많은 좋아요를 받은 리뷰 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MostLikedReview {
        private Long reviewId;
        private Long productId;
        private String productName;
        private String reviewContent;
        private Integer rating;
        private Long likeCount;
        private String authorName; // 마스킹된 이름
        private LocalDateTime createdAt;
    }

    /**
     * 활발한 좋아요 사용자 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopLiker {
        private Long userId;
        private String userName; // 마스킹된 이름
        private Long totalLikeCount;
        private Long activeLikeCount;
        private Double retentionRate;
        private LocalDateTime lastLikeDate;
        private String favoriteCategory;
    }

    /**
     * 좋아요를 많이 받는 상품 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularProductByLikes {
        private Long productId;
        private String productName;
        private String categoryName;
        private Long totalLikeCount;
        private Long reviewCount;
        private Double likesPerReview;
        private BigDecimal averageRating;
    }

    // === 정적 팩토리 메서드들 ===

    /**
     * 기본 통계 생성
     */
    public static ReviewLikeStatisticsDto createBasicStats() {
        return ReviewLikeStatisticsDto.builder()
            .totalLikeCount(0L)
            .activeLikeCount(0L)
            .cancelledLikeCount(0L)
            .todayLikeCount(0L)
            .monthlyLikeCount(0L)
            .likedReviewCount(0L)
            .uniqueLikerCount(0L)
            .activeLikerCount(0L)
            .averageLikesPerMember(0.0)
            .averageLikesPerReview(0.0)
            .cancellationRate(0.0)
            .retentionRate(0.0)
            .productsWithLikes(0L)
            .newLikersThisMonth(0L)
            .returningLikers(0L)
            .loyalLikers(0L)
            .averageDaysBetweenLikes(0.0)
            .stableLikeCount(0L)
            .stabilityScore(0.0)
            .toggleRate(0.0)
            .qualityIndex("N/A")
            .influenceScore(0.0)
            .engagementScore(0.0)
            .ecosystemHealth(0.0)
            .generatedAt(LocalDateTime.now())
            .dataAsOf(LocalDateTime.now())
            .build();
    }

    // === 유틸리티 메서드들 ===

    /**
     * 모든 비율 필드 계산
     */
    public void calculateAllRates() {
        calculateCancellationRate();
        calculateRetentionRate();
        calculateToggleRate();
        
        this.stabilityScore = calculateStabilityScore();
        this.engagementScore = calculateEngagementScore();
        this.ecosystemHealth = calculateEcosystemHealth();
        
        // 품질 지수 계산
        if (this.ecosystemHealth != null) {
            if (this.ecosystemHealth >= 90) this.qualityIndex = "최우수";
            else if (this.ecosystemHealth >= 80) this.qualityIndex = "우수";
            else if (this.ecosystemHealth >= 70) this.qualityIndex = "양호";
            else if (this.ecosystemHealth >= 60) this.qualityIndex = "보통";
            else this.qualityIndex = "개선 필요";
        }
    }

    /**
     * 개선 권장사항 생성
     */
    public List<String> getImprovementRecommendations() {
        List<String> recommendations = new java.util.ArrayList<>();
        
        if (cancellationRate != null && cancellationRate > 15) {
            recommendations.add("좋아요 취소율이 높습니다. 사용자 경험을 개선해보세요.");
        }
        
        if (averageLikesPerReview != null && averageLikesPerReview < 1.0) {
            recommendations.add("리뷰당 좋아요 수가 적습니다. 커뮤니티 참여를 독려해보세요.");
        }
        
        if (uniqueLikerCount != null && activeLikerCount != null && uniqueLikerCount > 0) {
            double activeRate = activeLikerCount.doubleValue() / uniqueLikerCount.doubleValue() * 100;
            if (activeRate < 30) {
                recommendations.add("활발한 좋아요 사용자 비율이 낮습니다. 사용자 참여 이벤트를 고려해보세요.");
            }
        }
        
        if (newLikersThisMonth != null && uniqueLikerCount != null && uniqueLikerCount > 0) {
            double newUserRate = newLikersThisMonth.doubleValue() / uniqueLikerCount.doubleValue() * 100;
            if (newUserRate < 20) {
                recommendations.add("신규 좋아요 사용자가 적습니다. 신규 사용자 유입을 늘려보세요.");
            }
        }
        
        if (ecosystemHealth != null && ecosystemHealth < 60) {
            recommendations.add("좋아요 생태계 전반적인 개선이 필요합니다.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("건강한 좋아요 생태계를 유지하고 있습니다!");
        }
        
        return recommendations;
    }

    /**
     * 성과 하이라이트 생성
     */
    public List<String> getPerformanceHighlights() {
        List<String> highlights = new java.util.ArrayList<>();
        
        if (retentionRate != null && retentionRate > 80) {
            highlights.add(String.format("높은 좋아요 유지율: %.1f%%", retentionRate));
        }
        
        if (averageLikesPerReview != null && averageLikesPerReview > 2.0) {
            highlights.add(String.format("우수한 리뷰당 좋아요: %.1f개", averageLikesPerReview));
        }
        
        if (todayLikeCount != null && todayLikeCount > 0) {
            highlights.add(String.format("오늘 %,d개 좋아요", todayLikeCount));
        }
        
        if (ecosystemHealth != null && ecosystemHealth > 80) {
            highlights.add(String.format("우수한 생태계 건전성: %.0f점", ecosystemHealth));
        }
        
        if (loyalLikers != null && uniqueLikerCount != null && uniqueLikerCount > 0) {
            double loyaltyRate = loyalLikers.doubleValue() / uniqueLikerCount.doubleValue() * 100;
            if (loyaltyRate > 20) {
                highlights.add(String.format("높은 사용자 충성도: %.1f%%", loyaltyRate));
            }
        }
        
        return highlights;
    }

    /**
     * 통계 요약 텍스트 생성
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append(String.format("총 %,d개 좋아요 중 %,d개(%.1f%%)가 활성 상태입니다. ",
            totalLikeCount != null ? totalLikeCount : 0,
            activeLikeCount != null ? activeLikeCount : 0,
            getActiveLikeRate() != null ? getActiveLikeRate() : 0.0));
        
        if (averageLikesPerReview != null) {
            summary.append(String.format("리뷰당 평균 %.1f개 좋아요를 받고 있으며, ", averageLikesPerReview));
        }
        
        if (retentionRate != null) {
            summary.append(String.format("좋아요 유지율은 %.1f%%입니다. ", retentionRate));
        }
        
        summary.append(String.format("생태계 건전성은 %.0f점으로 %s 등급입니다.",
            ecosystemHealth != null ? ecosystemHealth : calculateEcosystemHealth(),
            getEcosystemGrade()));
        
        return summary.toString();
    }

    /**
     * 좋아요 트렌드 분석
     */
    public String getLikeTrend() {
        if (quarterlyGrowthRate == null || quarterlyGrowthRate.isEmpty()) {
            return "트렌드 데이터 부족";
        }
        
        double averageGrowth = quarterlyGrowthRate.values().stream()
            .mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        if (averageGrowth > 15) return "빠른 성장";
        if (averageGrowth > 5) return "꾸준한 성장";
        if (averageGrowth > 0) return "완만한 성장";
        if (averageGrowth > -5) return "정체";
        return "감소 추세";
    }

    /**
     * 좋아요 활동 패턴 분석
     */
    public String getActivityPattern() {
        if (likesByHour == null || likesByHour.isEmpty()) {
            return "활동 패턴 데이터 없음";
        }
        
        // 피크 시간대 찾기
        int peakHour = likesByHour.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(12);
        
        if (peakHour >= 9 && peakHour <= 18) {
            return "주간 활동형";
        } else if (peakHour >= 19 && peakHour <= 23) {
            return "저녁 활동형";
        } else {
            return "심야 활동형";
        }
    }

    /**
     * 좋아요 품질 점수 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityScore {
        /**
         * 리뷰 ID
         */
        private Long reviewId;
        
        /**
         * 좋아요 ID (서비스에서 사용)
         */
        private Long likeId;  // 추가: 서비스에서 사용하는 필드
        
        /**
         * 품질 점수 (서비스에서 사용)
         */
        private Double qualityScore;  // 추가: 서비스에서 사용하는 필드
        
        /**
         * 전체 품질 점수 (0-100)
         */
        private Double totalScore;
        
        /**
         * 좋아요 속도 점수
         */
        private Double velocityScore;
        
        /**
         * 좋아요 지속성 점수
         */
        private Double persistenceScore;
        
        /**
         * 사용자 다양성 점수
         */
        private Double diversityScore;
        
        /**
         * 참여도 점수
         */
        private Double engagementScore;
        
        /**
         * 점수 계산 일시
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime calculatedAt;
        
        /**
         * 품질 등급 반환
         */
        public String getQualityGrade() {
            if (totalScore == null) return "N/A";
            if (totalScore >= 90) return "S";
            if (totalScore >= 80) return "A";
            if (totalScore >= 70) return "B";
            if (totalScore >= 60) return "C";
            if (totalScore >= 50) return "D";
            return "F";
        }
    }
}