package com.dodam.product.dto.statistics;

import com.dodam.product.entity.Review.ReviewStatus;
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
 * 리뷰 통계 DTO
 * 리뷰 관련 집계 데이터와 분석 정보를 제공하는 통계 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewStatisticsDto {

    // === 기본 집계 정보 ===

    /**
     * 전체 리뷰 수
     */
    private Long totalReviewCount;

    /**
     * 활성 리뷰 수 (공개된 리뷰)
     */
    private Long activeReviewCount;

    /**
     * 숨겨진 리뷰 수
     */
    private Long hiddenReviewCount;

    /**
     * 신고된 리뷰 수
     */
    private Long reportedReviewCount;

    /**
     * 이번 달 신규 리뷰 수
     */
    private Long monthlyNewReviews;

    /**
     * 오늘 작성된 리뷰 수
     */
    private Long todayReviews;

    // === 평점 관련 통계 ===

    /**
     * 전체 평균 평점
     */
    private BigDecimal averageRating;

    /**
     * 평점 중앙값
     */
    private BigDecimal medianRating;

    /**
     * 최고 평점
     */
    private BigDecimal maxRating;

    /**
     * 최저 평점
     */
    private BigDecimal minRating;

    /**
     * 평점 표준편차
     */
    private BigDecimal ratingStandardDeviation;

    // === 평점 분포 ===

    /**
     * 평점별 리뷰 수 분포 (1점~5점)
     */
    private Map<Integer, Long> ratingDistribution;

    /**
     * 평점별 비율 (백분율)
     */
    private Map<Integer, Double> ratingPercentage;

    /**
     * 긍정적 리뷰 수 (4-5점)
     */
    private Long positiveReviewCount;

    /**
     * 중립적 리뷰 수 (3점)
     */
    private Long neutralReviewCount;

    /**
     * 부정적 리뷰 수 (1-2점)
     */
    private Long negativeReviewCount;

    // === 리뷰 품질 지표 ===

    /**
     * 평균 리뷰 길이 (글자 수)
     */
    private Double averageReviewLength;

    /**
     * 상세 리뷰 수 (100자 이상)
     */
    private Long detailedReviewCount;

    /**
     * 짧은 리뷰 수 (20자 미만)
     */
    private Long shortReviewCount;

    /**
     * 이미지 첨부 리뷰 수
     */
    private Long reviewsWithImages;

    /**
     * 검증된 구매 리뷰 수
     */
    private Long verifiedPurchaseReviews;

    // === 상호작용 통계 ===

    /**
     * 총 리뷰 좋아요 수
     */
    private Long totalLikes;

    /**
     * 좋아요를 받은 리뷰 수
     */
    private Long likedReviewCount;

    /**
     * 리뷰당 평균 좋아요 수
     */
    private Double averageLikesPerReview;

    /**
     * 가장 인기 있는 리뷰 (좋아요 순)
     */
    private List<PopularReview> topLikedReviews;

    // === 상품별 리뷰 분석 ===

    /**
     * 리뷰가 있는 상품 수
     */
    private Long reviewedProductCount;

    /**
     * 상품당 평균 리뷰 수
     */
    private Double averageReviewsPerProduct;

    /**
     * 리뷰가 많은 상품 TOP 10
     */
    private List<MostReviewedProduct> mostReviewedProducts;

    /**
     * 평점이 높은 상품 TOP 10
     */
    private List<TopRatedProduct> topRatedProducts;

    // === 사용자 참여 분석 ===

    /**
     * 리뷰 작성자 수 (유니크 사용자)
     */
    private Long uniqueReviewers;

    /**
     * 활발한 리뷰어 수 (3개 이상 리뷰 작성)
     */
    private Long activeReviewers;

    /**
     * 사용자당 평균 리뷰 수
     */
    private Double averageReviewsPerUser;

    /**
     * 가장 활발한 리뷰어 TOP 10
     */
    private List<TopReviewer> topReviewers;

    // === 시계열 데이터 ===

    /**
     * 월별 리뷰 수 (최근 12개월)
     */
    private Map<String, Long> monthlyReviewCounts;

    /**
     * 일별 리뷰 수 (최근 30일)
     */
    private Map<String, Long> dailyReviewCounts;

    /**
     * 월별 평균 평점 (최근 12개월)
     */
    private Map<String, BigDecimal> monthlyAverageRatings;

    /**
     * 분기별 성장률 (%)
     */
    private Map<String, Double> quarterlyGrowthRate;

    // === 감정 분석 (선택적) ===

    /**
     * 긍정적 감정 리뷰 비율 (%)
     */
    private Double positiveSentimentRate;

    /**
     * 부정적 감정 리뷰 비율 (%)
     */
    private Double negativeSentimentRate;

    /**
     * 중립적 감정 리뷰 비율 (%)
     */
    private Double neutralSentimentRate;

    // === 카테고리별 분석 ===

    /**
     * 카테고리별 리뷰 수
     */
    private Map<String, Long> reviewsByCategory;

    /**
     * 카테고리별 평균 평점
     */
    private Map<String, BigDecimal> averageRatingByCategory;

    /**
     * 카테고리별 리뷰 참여율
     */
    private Map<String, Double> participationRateByCategory;

    // === 리뷰 상태별 분포 ===

    /**
     * 상태별 리뷰 수 분포
     */
    private Map<ReviewStatus, Long> reviewsByStatus;

    /**
     * 상태별 비율 (백분율)
     */
    private Map<ReviewStatus, Double> statusDistribution;

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
     * 활성 리뷰 비율 (%)
     */
    public Double getActiveReviewRate() {
        if (totalReviewCount == null || totalReviewCount == 0) {
            return 0.0;
        }
        return (activeReviewCount != null) ? 
            (activeReviewCount.doubleValue() / totalReviewCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 긍정적 리뷰 비율 (%)
     */
    public Double getPositiveReviewRate() {
        if (totalReviewCount == null || totalReviewCount == 0) {
            return 0.0;
        }
        return (positiveReviewCount != null) ? 
            (positiveReviewCount.doubleValue() / totalReviewCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 부정적 리뷰 비율 (%)
     */
    public Double getNegativeReviewRate() {
        if (totalReviewCount == null || totalReviewCount == 0) {
            return 0.0;
        }
        return (negativeReviewCount != null) ? 
            (negativeReviewCount.doubleValue() / totalReviewCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 상세 리뷰 비율 (%)
     */
    public Double getDetailedReviewRate() {
        if (totalReviewCount == null || totalReviewCount == 0) {
            return 0.0;
        }
        return (detailedReviewCount != null) ? 
            (detailedReviewCount.doubleValue() / totalReviewCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 이미지 첨부 리뷰 비율 (%)
     */
    public Double getImageReviewRate() {
        if (totalReviewCount == null || totalReviewCount == 0) {
            return 0.0;
        }
        return (reviewsWithImages != null) ? 
            (reviewsWithImages.doubleValue() / totalReviewCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 리뷰 참여율 계산 (리뷰가 있는 상품 비율)
     */
    public Double getParticipationRate() {
        // 이 값은 외부에서 전체 상품 수와 함께 계산되어 설정되어야 함
        return participationRateByCategory != null && !participationRateByCategory.isEmpty() ?
            participationRateByCategory.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0) : 0.0;
    }

    /**
     * 평점 품질 지수 계산 (평점 분포의 건전성)
     */
    public String getRatingQualityIndex() {
        if (ratingDistribution == null || ratingDistribution.isEmpty()) {
            return "N/A";
        }
        
        // 평점 분포의 균등성 검사
        Long total = ratingDistribution.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) return "N/A";
        
        // 극단적 평점 (1점, 5점)의 비율
        Long extreme = ratingDistribution.getOrDefault(1, 0L) + ratingDistribution.getOrDefault(5, 0L);
        double extremeRate = extreme.doubleValue() / total.doubleValue() * 100;
        
        if (extremeRate < 30) return "매우 건전";
        if (extremeRate < 50) return "건전";
        if (extremeRate < 70) return "보통";
        if (extremeRate < 85) return "편향됨";
        return "매우 편향됨";
    }

    /**
     * 리뷰 품질 등급
     */
    public String getQualityGrade() {
        double score = calculateQualityScore();
        
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        return "D";
    }

    // === 내부 클래스들 ===

    /**
     * 인기 리뷰 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularReview {
        private Long reviewId;
        private Long productId;
        private String productName;
        private Integer rating;
        private String content;
        private Long likeCount;
        private String reviewerName; // 마스킹된 이름
        private LocalDateTime createdAt;
    }

    /**
     * 리뷰가 많은 상품 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MostReviewedProduct {
        private Long productId;
        private String productName;
        private String categoryName;
        private Long reviewCount;
        private BigDecimal averageRating;
        private BigDecimal price;
    }

    /**
     * 평점이 높은 상품 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopRatedProduct {
        private Long productId;
        private String productName;
        private String categoryName;
        private BigDecimal averageRating;
        private Long reviewCount;
        private BigDecimal price;
    }

    /**
     * 활발한 리뷰어 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopReviewer {
        private Long userId;
        private String reviewerName; // 마스킹된 이름
        private Long reviewCount;
        private BigDecimal averageRatingGiven;
        private Long totalLikesReceived;
        private LocalDateTime lastReviewDate;
    }

    // === 정적 팩토리 메서드들 ===

    /**
     * 기본 통계 생성
     */
    public static ReviewStatisticsDto createBasicStats() {
        return ReviewStatisticsDto.builder()
            .totalReviewCount(0L)
            .activeReviewCount(0L)
            .hiddenReviewCount(0L)
            .reportedReviewCount(0L)
            .monthlyNewReviews(0L)
            .todayReviews(0L)
            .averageRating(BigDecimal.ZERO)
            .medianRating(BigDecimal.ZERO)
            .maxRating(BigDecimal.ZERO)
            .minRating(BigDecimal.ZERO)
            .ratingStandardDeviation(BigDecimal.ZERO)
            .positiveReviewCount(0L)
            .neutralReviewCount(0L)
            .negativeReviewCount(0L)
            .averageReviewLength(0.0)
            .detailedReviewCount(0L)
            .shortReviewCount(0L)
            .reviewsWithImages(0L)
            .verifiedPurchaseReviews(0L)
            .totalLikes(0L)
            .likedReviewCount(0L)
            .averageLikesPerReview(0.0)
            .reviewedProductCount(0L)
            .averageReviewsPerProduct(0.0)
            .uniqueReviewers(0L)
            .activeReviewers(0L)
            .averageReviewsPerUser(0.0)
            .positiveSentimentRate(0.0)
            .negativeSentimentRate(0.0)
            .neutralSentimentRate(0.0)
            .generatedAt(LocalDateTime.now())
            .dataAsOf(LocalDateTime.now())
            .build();
    }

    // === 유틸리티 메서드들 ===

    /**
     * 건강한 리뷰 생태계인지 확인
     */
    public boolean isHealthyEcosystem() {
        double healthScore = calculateHealthScore();
        return healthScore >= 70.0;
    }

    /**
     * 리뷰 생태계 건강도 점수 계산 (0-100)
     */
    public double calculateHealthScore() {
        double score = 0.0;
        
        // 활성 리뷰 비율 (20점 만점)
        Double activeRate = getActiveReviewRate();
        if (activeRate != null) {
            score += Math.min(activeRate * 0.2, 20.0);
        }
        
        // 긍정적 리뷰 비율 (15점 만점)
        Double positiveRate = getPositiveReviewRate();
        if (positiveRate != null) {
            score += Math.min(positiveRate * 0.15, 15.0);
        }
        
        // 상세 리뷰 비율 (20점 만점)
        Double detailedRate = getDetailedReviewRate();
        if (detailedRate != null) {
            score += Math.min(detailedRate * 0.2, 20.0);
        }
        
        // 참여율 (15점 만점)
        Double participation = getParticipationRate();
        if (participation != null) {
            score += Math.min(participation * 0.15, 15.0);
        }
        
        // 평점 품질 (15점 만점)
        if (averageRating != null) {
            score += averageRating.doubleValue() * 3.0; // 5점 만점을 15점으로 변환
        }
        
        // 상호작용 정도 (15점 만점) - 좋아요 받은 리뷰 비율
        if (totalReviewCount != null && totalReviewCount > 0 && likedReviewCount != null) {
            double likeRate = likedReviewCount.doubleValue() / totalReviewCount.doubleValue() * 100;
            score += Math.min(likeRate * 0.15, 15.0);
        }
        
        return Math.min(score, 100.0);
    }

    /**
     * 리뷰 품질 점수 계산 (0-100)
     */
    private double calculateQualityScore() {
        double score = 0.0;
        
        // 상세 리뷰 비율 (30점 만점)
        Double detailedRate = getDetailedReviewRate();
        if (detailedRate != null) {
            score += detailedRate * 0.3;
        }
        
        // 이미지 첨부 비율 (20점 만점)
        Double imageRate = getImageReviewRate();
        if (imageRate != null) {
            score += imageRate * 0.2;
        }
        
        // 검증된 구매 리뷰 비율 (25점 만점)
        if (totalReviewCount != null && totalReviewCount > 0 && verifiedPurchaseReviews != null) {
            double verifiedRate = verifiedPurchaseReviews.doubleValue() / totalReviewCount.doubleValue() * 100;
            score += verifiedRate * 0.25;
        }
        
        // 활성 리뷰 비율 (15점 만점)
        Double activeRate = getActiveReviewRate();
        if (activeRate != null) {
            score += activeRate * 0.15;
        }
        
        // 평균 좋아요 수 (10점 만점)
        if (averageLikesPerReview != null) {
            score += Math.min(averageLikesPerReview * 2, 10.0);
        }
        
        return Math.min(score, 100.0);
    }

    /**
     * 개선 권장사항 생성
     */
    public List<String> getImprovementRecommendations() {
        List<String> recommendations = new java.util.ArrayList<>();
        
        Double positiveRate = getPositiveReviewRate();
        if (positiveRate != null && positiveRate < 60) {
            recommendations.add("긍정적 리뷰 비율이 낮습니다. 제품 품질 개선을 고려해보세요.");
        }
        
        Double detailedRate = getDetailedReviewRate();
        if (detailedRate != null && detailedRate < 40) {
            recommendations.add("상세한 리뷰가 부족합니다. 리뷰 가이드라인을 제공해보세요.");
        }
        
        Double imageRate = getImageReviewRate();
        if (imageRate != null && imageRate < 20) {
            recommendations.add("이미지 첨부 리뷰가 적습니다. 이미지 업로드를 장려해보세요.");
        }
        
        Double participation = getParticipationRate();
        if (participation != null && participation < 30) {
            recommendations.add("리뷰 참여율이 낮습니다. 리뷰 작성 인센티브를 제공해보세요.");
        }
        
        if (reportedReviewCount != null && totalReviewCount != null && totalReviewCount > 0) {
            double reportRate = reportedReviewCount.doubleValue() / totalReviewCount.doubleValue() * 100;
            if (reportRate > 5) {
                recommendations.add("신고된 리뷰가 많습니다. 리뷰 품질 관리를 강화해보세요.");
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("전반적으로 건강한 리뷰 생태계를 유지하고 있습니다!");
        }
        
        return recommendations;
    }

    /**
     * 통계 요약 텍스트 생성
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append(String.format("총 %,d개 리뷰 중 %,d개(%.1f%%)가 활성 상태입니다. ",
            totalReviewCount != null ? totalReviewCount : 0,
            activeReviewCount != null ? activeReviewCount : 0,
            getActiveReviewRate() != null ? getActiveReviewRate() : 0.0));
        
        if (averageRating != null) {
            summary.append(String.format("평균 평점은 %.1f점이며, ", averageRating.doubleValue()));
        }
        
        Double positiveRate = getPositiveReviewRate();
        if (positiveRate != null) {
            summary.append(String.format("%.1f%%가 긍정적 리뷰입니다. ", positiveRate));
        }
        
        summary.append(String.format("리뷰 생태계 건강도는 %.0f점으로 %s 상태입니다.",
            calculateHealthScore(),
            isHealthyEcosystem() ? "건전한" : "개선이 필요한"));
        
        return summary.toString();
    }
}