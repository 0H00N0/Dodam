package com.dodam.product.dto.response;

import com.dodam.product.entity.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 카테고리 응답 DTO
 * 카테고리 조회 결과를 클라이언트에 전달할 때 사용됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDto {

    /**
     * 카테고리 고유 번호
     */
    private Long categoryId;

    /**
     * 카테고리 이름
     */
    private String categoryName;

    /**
     * 카테고리 설명
     */
    private String description;

    /**
     * 이 카테고리에 속한 상품 개수
     */
    private Integer productCount;

    /**
     * 이 카테고리에 속한 활성 상품 개수 (판매중인 상품)
     */
    private Integer activeProductCount;

    /**
     * 카테고리 생성일시
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    /**
     * 카테고리 수정일시
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    /**
     * 카테고리 삭제 여부
     */
    private Boolean isDeleted;

    /**
     * 카테고리에 속한 상품들의 최저가격
     */
    private String minPrice;

    /**
     * 카테고리에 속한 상품들의 최고가격
     */
    private String maxPrice;

    /**
     * 카테고리에 속한 상품들의 평균 평점
     */
    private Double averageRating;

    /**
     * 카테고리 상태 텍스트 (삭제됨, 정상 등)
     */
    private String statusText;

    /**
     * Entity를 ResponseDto로 변환하는 메소드 (기본 정보만)
     * 
     * @param category 카테고리 엔티티
     * @return CategoryResponseDto 객체
     */
    public static CategoryResponseDto fromEntity(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponseDto.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .isDeleted(category.isDeleted())
                .statusText(category.isDeleted() ? "삭제됨" : "정상")
                .productCount(0) // 기본값, 별도 계산 필요
                .activeProductCount(0) // 기본값, 별도 계산 필요
                .build();
    }

    /**
     * Entity를 ResponseDto로 변환하는 메소드 (상품 정보 포함)
     * 
     * @param category 카테고리 엔티티
     * @param includeProductInfo 상품 정보 포함 여부
     * @return CategoryResponseDto 객체
     */
    public static CategoryResponseDto fromEntity(Category category, boolean includeProductInfo) {
        if (category == null) {
            return null;
        }

        CategoryResponseDto dto = fromEntity(category);
        
        if (includeProductInfo && category.getProducts() != null) {
            // 상품 통계 정보 계산
            int totalProducts = category.getProducts().size();
            int activeProducts = (int) category.getProducts().stream()
                    .filter(product -> !product.isDeleted() && product.getStatus().isAvailable())
                    .count();
            
            dto.setProductCount(totalProducts);
            dto.setActiveProductCount(activeProducts);
            
            // 가격 범위 계산
            var prices = category.getProducts().stream()
                    .filter(product -> !product.isDeleted() && product.getPrice() != null)
                    .map(product -> product.getPrice())
                    .collect(Collectors.toList());
            
            if (!prices.isEmpty()) {
                var minPrice = prices.stream().min(java.math.BigDecimal::compareTo).orElse(null);
                var maxPrice = prices.stream().max(java.math.BigDecimal::compareTo).orElse(null);
                
                dto.setMinPrice(minPrice != null ? minPrice.toString() + "원" : null);
                dto.setMaxPrice(maxPrice != null ? maxPrice.toString() + "원" : null);
            }
            
            // 평균 평점 계산
            var ratings = category.getProducts().stream()
                    .filter(product -> !product.isDeleted())
                    .mapToDouble(product -> product.getAverageRating())
                    .filter(rating -> rating > 0)
                    .average();
            
            dto.setAverageRating(ratings.isPresent() ? Math.round(ratings.getAsDouble() * 10) / 10.0 : 0.0);
        }
        
        return dto;
    }

    /**
     * Entity 목록을 ResponseDto 목록으로 변환하는 메소드
     * 
     * @param categories 카테고리 엔티티 목록
     * @return CategoryResponseDto 목록
     */
    public static List<CategoryResponseDto> fromEntityList(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }

        return categories.stream()
                .map(CategoryResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Entity 목록을 ResponseDto 목록으로 변환하는 메소드 (상품 정보 포함)
     * 
     * @param categories 카테고리 엔티티 목록
     * @param includeProductInfo 상품 정보 포함 여부
     * @return CategoryResponseDto 목록
     */
    public static List<CategoryResponseDto> fromEntityList(List<Category> categories, boolean includeProductInfo) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }

        return categories.stream()
                .map(category -> fromEntity(category, includeProductInfo))
                .collect(Collectors.toList());
    }

    /**
     * 요약 정보만 포함한 간단한 ResponseDto 생성
     * 
     * @param category 카테고리 엔티티
     * @return 간단한 CategoryResponseDto 객체
     */
    public static CategoryResponseDto toSummary(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponseDto.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .isDeleted(category.isDeleted())
                .statusText(category.isDeleted() ? "삭제됨" : "정상")
                .build();
    }

    /**
     * 카테고리가 활성 상태인지 확인
     * 
     * @return 활성 상태 여부
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(isDeleted);
    }

    /**
     * 카테고리에 상품이 있는지 확인
     * 
     * @return 상품 존재 여부
     */
    public boolean hasProducts() {
        return productCount != null && productCount > 0;
    }

    /**
     * 카테고리에 판매중인 상품이 있는지 확인
     * 
     * @return 판매중인 상품 존재 여부
     */
    public boolean hasActiveProducts() {
        return activeProductCount != null && activeProductCount > 0;
    }

    /**
     * 가격 정보가 있는지 확인
     * 
     * @return 가격 정보 존재 여부
     */
    public boolean hasPriceInfo() {
        return minPrice != null && maxPrice != null;
    }

    /**
     * 평점 정보가 있는지 확인
     * 
     * @return 평점 정보 존재 여부
     */
    public boolean hasRatingInfo() {
        return averageRating != null && averageRating > 0;
    }

    /**
     * 카테고리 통계 요약 텍스트 생성
     * 
     * @return 통계 요약 텍스트
     */
    public String getStatsSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (hasActiveProducts()) {
            summary.append(String.format("상품 %d개", activeProductCount));
        } else {
            summary.append("상품 없음");
        }
        
        if (hasRatingInfo()) {
            summary.append(String.format(" | 평점 %.1f", averageRating));
        }
        
        if (hasPriceInfo() && !minPrice.equals(maxPrice)) {
            summary.append(String.format(" | %s~%s", minPrice, maxPrice));
        } else if (hasPriceInfo()) {
            summary.append(String.format(" | %s", minPrice));
        }
        
        return summary.toString();
    }

    /**
     * 관리자용 상세 정보 포함 ResponseDto 생성
     * 
     * @param category 카테고리 엔티티
     * @param includeProductInfo 상품 정보 포함 여부
     * @return 관리자용 CategoryResponseDto 객체
     */
    public static CategoryResponseDto forAdmin(Category category, boolean includeProductInfo) {
        CategoryResponseDto dto = fromEntity(category, includeProductInfo);
        
        if (dto != null) {
            // 관리자용 추가 정보 설정
            dto.setStatusText(category.isDeleted() ? 
                String.format("삭제됨 (%s)", category.getDeletedAt().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))) : 
                "정상");
        }
        
        return dto;
    }

    /**
     * 목록 페이지용 간소화된 정보 포함 ResponseDto 생성
     * 
     * @param category 카테고리 엔티티
     * @return 목록용 CategoryResponseDto 객체
     */
    public static CategoryResponseDto forList(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponseDto.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription() != null && category.getDescription().length() > 100 ?
                           category.getDescription().substring(0, 100) + "..." :
                           category.getDescription())
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .activeProductCount(category.getProducts() != null ? 
                                  (int) category.getProducts().stream()
                                          .filter(p -> !p.isDeleted() && p.getStatus().isAvailable())
                                          .count() : 0)
                .createdAt(category.getCreatedAt())
                .isDeleted(category.isDeleted())
                .statusText(category.isDeleted() ? "삭제됨" : "정상")
                .build();
    }

    @Override
    public String toString() {
        return String.format("CategoryResponseDto(id=%d, name=%s, productCount=%d, isDeleted=%s)", 
                           categoryId, categoryName, productCount, isDeleted);
    }
}