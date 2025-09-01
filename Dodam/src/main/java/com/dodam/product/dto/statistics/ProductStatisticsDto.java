package com.dodam.product.dto.statistics;

import com.dodam.product.entity.Product.ProductStatus;
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
 * 상품 통계 DTO
 * 상품 관련 집계 데이터와 분석 정보를 제공하는 통계 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductStatisticsDto {

    // === 기본 집계 정보 ===

    /**
     * 전체 상품 수
     */
    private Long totalProductCount;

    /**
     * 활성 상품 수
     */
    private Long activeProductCount;

    /**
     * 비활성 상품 수
     */
    private Long inactiveProductCount;

    /**
     * 품절 상품 수
     */
    private Long outOfStockCount;

    /**
     * 신규 상품 수 (최근 30일)
     */
    private Long newProductCount;

    /**
     * 삭제된 상품 수
     */
    private Long deletedProductCount;

    // === 가격 관련 통계 ===

    /**
     * 전체 상품 평균 가격
     */
    private BigDecimal averagePrice;

    /**
     * 최고 가격
     */
    private BigDecimal maxPrice;

    /**
     * 최저 가격
     */
    private BigDecimal minPrice;

    /**
     * 가격 중앙값
     */
    private BigDecimal medianPrice;

    /**
     * 총 상품 가치 (모든 상품 가격 합계)
     */
    private BigDecimal totalValue;

    // === 카테고리별 통계 ===

    /**
     * 카테고리별 상품 수
     */
    private Map<String, Long> productsByCategory;

    /**
     * 카테고리별 평균 가격
     */
    private Map<String, BigDecimal> averagePriceByCategory;

    /**
     * 카테고리별 활성 상품 비율
     */
    private Map<String, Double> activeRateByCategory;

    // === 상태별 분포 ===

    /**
     * 상태별 상품 수 분포
     */
    private Map<ProductStatus, Long> productsByStatus;

    /**
     * 상태별 비율 (백분율)
     */
    private Map<ProductStatus, Double> statusDistribution;

    // === 성과 지표 ===

    /**
     * 상품별 평점 평균
     */
    private BigDecimal averageRating;

    /**
     * 리뷰가 있는 상품 수
     */
    private Long reviewedProductCount;

    /**
     * 상품별 평균 리뷰 수
     */
    private Double averageReviewCount;

    /**
     * 인기 상품 TOP 10 (평점 기준)
     */
    private List<PopularProduct> topRatedProducts;

    /**
     * 인기 상품 TOP 10 (리뷰 수 기준)
     */
    private List<PopularProduct> mostReviewedProducts;

    // === 가격대별 분석 ===

    /**
     * 가격대별 상품 분포
     * 키: 가격대 레이블 (예: "10,000원 미만", "10,000-50,000원" 등)
     * 값: 해당 가격대 상품 수
     */
    private Map<String, Long> priceRangeDistribution;

    /**
     * 프리미엄 상품 수 (상위 20% 가격)
     */
    private Long premiumProductCount;

    /**
     * 예산형 상품 수 (하위 20% 가격)
     */
    private Long budgetProductCount;

    // === 시계열 데이터 ===

    /**
     * 월별 신규 상품 등록 수 (최근 12개월)
     */
    private Map<String, Long> monthlyNewProducts;

    /**
     * 일별 상품 등록 수 (최근 30일)
     */
    private Map<String, Long> dailyNewProducts;

    /**
     * 분기별 성장률 (%)
     */
    private Map<String, Double> quarterlyGrowthRate;

    // === 품질 지표 ===

    /**
     * 완전한 정보를 가진 상품 수 (제목, 설명, 이미지, 가격 모두 있음)
     */
    private Long completeProductCount;

    /**
     * 정보 완성도 평균 (%)
     */
    private Double averageCompleteness;

    /**
     * 이미지가 있는 상품 수
     */
    private Long productsWithImages;

    /**
     * 설명이 있는 상품 수
     */
    private Long productsWithDescription;

    // === 메타데이터 ===

    /**
     * 통계 생성 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime generatedAt;

    /**
     * 데이터 기준 일시 (마지막 업데이트)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime dataAsOf;

    /**
     * 통계 생성에 소요된 시간 (밀리초)
     */
    private Long processingTimeMs;

    // === 계산된 필드들 ===

    /**
     * 활성 상품 비율 (%)
     */
    public Double getActiveProductRate() {
        if (totalProductCount == null || totalProductCount == 0) {
            return 0.0;
        }
        return (activeProductCount != null) ? 
            (activeProductCount.doubleValue() / totalProductCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 품절률 (%)
     */
    public Double getOutOfStockRate() {
        if (totalProductCount == null || totalProductCount == 0) {
            return 0.0;
        }
        return (outOfStockCount != null) ? 
            (outOfStockCount.doubleValue() / totalProductCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 신규 상품 비율 (%)
     */
    public Double getNewProductRate() {
        if (totalProductCount == null || totalProductCount == 0) {
            return 0.0;
        }
        return (newProductCount != null) ? 
            (newProductCount.doubleValue() / totalProductCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 리뷰 보유 상품 비율 (%)
     */
    public Double getReviewCoverage() {
        if (totalProductCount == null || totalProductCount == 0) {
            return 0.0;
        }
        return (reviewedProductCount != null) ? 
            (reviewedProductCount.doubleValue() / totalProductCount.doubleValue() * 100) : 0.0;
    }

    /**
     * 정보 완성도 등급 반환
     */
    public String getCompletenessGrade() {
        if (averageCompleteness == null) {
            return "N/A";
        }
        
        if (averageCompleteness >= 90) return "A+";
        if (averageCompleteness >= 80) return "A";
        if (averageCompleteness >= 70) return "B";
        if (averageCompleteness >= 60) return "C";
        return "D";
    }

    /**
     * 상품 다양성 지수 계산 (카테고리 수 기반)
     */
    public Integer getDiversityIndex() {
        if (productsByCategory == null || productsByCategory.isEmpty()) {
            return 0;
        }
        
        // 카테고리별 상품 분포의 균등성을 측정
        double totalProducts = productsByCategory.values().stream()
            .mapToLong(Long::longValue).sum();
        
        if (totalProducts == 0) return 0;
        
        // Shannon Diversity Index 계산
        double diversity = 0.0;
        for (Long count : productsByCategory.values()) {
            if (count > 0) {
                double proportion = count / totalProducts;
                diversity -= proportion * Math.log(proportion) / Math.log(2);
            }
        }
        
        // 0-100 스케일로 정규화
        return (int) (diversity / Math.log(productsByCategory.size()) * 100);
    }

    /**
     * 가격 안정성 지수 계산
     */
    public String getPriceStabilityIndex() {
        if (averagePrice == null || minPrice == null || maxPrice == null) {
            return "N/A";
        }
        
        if (maxPrice.equals(minPrice)) {
            return "매우 안정";
        }
        
        // 변동계수 계산 (표준편차 / 평균)
        BigDecimal range = maxPrice.subtract(minPrice);
        BigDecimal coefficient = range.divide(averagePrice, 2, RoundingMode.HALF_UP);
        
        if (coefficient.compareTo(new BigDecimal("0.2")) <= 0) return "매우 안정";
        if (coefficient.compareTo(new BigDecimal("0.5")) <= 0) return "안정";
        if (coefficient.compareTo(new BigDecimal("1.0")) <= 0) return "보통";
        if (coefficient.compareTo(new BigDecimal("2.0")) <= 0) return "변동성 높음";
        return "매우 높음";
    }

    // === 내부 클래스들 ===

    /**
     * 카테고리 통계 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStats {
        /**
         * 전체 카테고리 수
         */
        private Long totalCategories;
        
        /**
         * 상품이 있는 카테고리 수
         */
        private Long categoriesWithProducts;
        
        /**
         * 빈 카테고리 수
         */
        private Long emptyCategories;
    }

    /**
     * 카테고리별 상품 수 통계
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryProductCount {
        /**
         * 카테고리명
         */
        private String categoryName;
        
        /**
         * 상품 수
         */
        private Long productCount;
    }

    /**
     * 인기 상품 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularProduct {
        
        /**
         * 상품 ID
         */
        private Long productId;
        
        /**
         * 상품명
         */
        private String productName;
        
        /**
         * 카테고리명
         */
        private String categoryName;
        
        /**
         * 가격
         */
        private BigDecimal price;
        
        /**
         * 평균 평점
         */
        private BigDecimal rating;
        
        /**
         * 리뷰 수
         */
        private Long reviewCount;
        
        /**
         * 순위
         */
        private Integer rank;
        
        /**
         * 인기도 점수
         */
        private Double popularityScore;
    }

    // === 정적 팩토리 메서드들 ===

    /**
     * 기본 통계 생성
     */
    public static ProductStatisticsDto createBasicStats() {
        return ProductStatisticsDto.builder()
            .totalProductCount(0L)
            .activeProductCount(0L)
            .inactiveProductCount(0L)
            .outOfStockCount(0L)
            .newProductCount(0L)
            .deletedProductCount(0L)
            .averagePrice(BigDecimal.ZERO)
            .maxPrice(BigDecimal.ZERO)
            .minPrice(BigDecimal.ZERO)
            .medianPrice(BigDecimal.ZERO)
            .totalValue(BigDecimal.ZERO)
            .averageRating(BigDecimal.ZERO)
            .reviewedProductCount(0L)
            .averageReviewCount(0.0)
            .completeProductCount(0L)
            .averageCompleteness(0.0)
            .productsWithImages(0L)
            .productsWithDescription(0L)
            .generatedAt(LocalDateTime.now())
            .dataAsOf(LocalDateTime.now())
            .build();
    }

    // === 유틸리티 메서드들 ===

    /**
     * 건강한 상품 카탈로그인지 확인 (종합 점수 70점 이상)
     */
    public boolean isHealthyCatalog() {
        double healthScore = calculateHealthScore();
        return healthScore >= 70.0;
    }

    /**
     * 카탈로그 건강도 점수 계산 (0-100)
     */
    public double calculateHealthScore() {
        double score = 0.0;
        
        // 활성 상품 비율 (30점 만점)
        Double activeRate = getActiveProductRate();
        if (activeRate != null) {
            score += Math.min(activeRate * 0.3, 30.0);
        }
        
        // 정보 완성도 (25점 만점)
        if (averageCompleteness != null) {
            score += averageCompleteness * 0.25;
        }
        
        // 리뷰 보유율 (20점 만점)
        Double reviewCoverage = getReviewCoverage();
        if (reviewCoverage != null) {
            score += Math.min(reviewCoverage * 0.2, 20.0);
        }
        
        // 다양성 지수 (15점 만점)
        Integer diversity = getDiversityIndex();
        if (diversity != null) {
            score += diversity * 0.15;
        }
        
        // 품질 점수 (10점 만점) - 평균 평점 기준
        if (averageRating != null) {
            score += averageRating.doubleValue() * 2.0; // 5점 만점을 10점으로 변환
        }
        
        return Math.min(score, 100.0);
    }

    /**
     * 개선 권장사항 생성
     */
    public List<String> getImprovementRecommendations() {
        List<String> recommendations = new java.util.ArrayList<>();
        
        Double activeRate = getActiveProductRate();
        if (activeRate != null && activeRate < 60) {
            recommendations.add("활성 상품 비율이 낮습니다. 비활성 상품을 검토하고 정리해보세요.");
        }
        
        Double outOfStockRate = getOutOfStockRate();
        if (outOfStockRate != null && outOfStockRate > 20) {
            recommendations.add("품절 상품이 많습니다. 재고 관리를 강화해보세요.");
        }
        
        if (averageCompleteness != null && averageCompleteness < 80) {
            recommendations.add("상품 정보 완성도가 낮습니다. 누락된 정보를 보완해보세요.");
        }
        
        Double reviewCoverage = getReviewCoverage();
        if (reviewCoverage != null && reviewCoverage < 30) {
            recommendations.add("리뷰가 없는 상품이 많습니다. 리뷰 작성을 독려해보세요.");
        }
        
        Integer diversity = getDiversityIndex();
        if (diversity != null && diversity < 50) {
            recommendations.add("상품 다양성이 부족합니다. 새로운 카테고리 확장을 고려해보세요.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("전반적으로 건강한 상품 카탈로그를 유지하고 있습니다!");
        }
        
        return recommendations;
    }

    /**
     * 통계 요약 텍스트 생성
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append(String.format("전체 %,d개 상품 중 %,d개(%.1f%%)가 활성 상태입니다. ",
            totalProductCount != null ? totalProductCount : 0,
            activeProductCount != null ? activeProductCount : 0,
            getActiveProductRate() != null ? getActiveProductRate() : 0.0));
        
        if (averagePrice != null) {
            summary.append(String.format("평균 가격은 %,d원이며, ", averagePrice.intValue()));
        }
        
        if (averageRating != null) {
            summary.append(String.format("평균 평점은 %.1f점입니다. ", averageRating.doubleValue()));
        }
        
        summary.append(String.format("카탈로그 건강도는 %.0f점으로 %s 상태입니다.",
            calculateHealthScore(),
            isHealthyCatalog() ? "양호한" : "개선이 필요한"));
        
        return summary.toString();
    }
}