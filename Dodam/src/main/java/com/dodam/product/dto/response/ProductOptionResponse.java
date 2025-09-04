package com.dodam.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 옵션 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionResponse {

    /**
     * 옵션 ID
     */
    private Long optionId;

    /**
     * 상품 ID
     */
    private Long productId;

    /**
     * 상품명
     */
    private String productName;

    /**
     * 옵션 그룹 (예: "색상", "사이즈")
     */
    private String optionGroup;

    /**
     * 옵션명 (예: "빨간색", "XL")
     */
    private String optionName;

    /**
     * 옵션 값 (예: "RED", "XL")
     */
    private String optionValue;

    /**
     * 기본 가격
     */
    private BigDecimal basePrice;

    /**
     * 추가 가격
     */
    private BigDecimal additionalPrice;

    /**
     * 총 가격 (기본 가격 + 추가 가격)
     */
    private BigDecimal totalPrice;

    /**
     * 재고 수량
     */
    private Integer stockQuantity;

    /**
     * 판매 가능 여부 (재고가 있고 활성화된 상태)
     */
    private Boolean isAvailable;

    /**
     * 옵션 순서
     */
    private Integer sortOrder;

    /**
     * 옵션 활성화 여부
     */
    private Boolean isActive;

    /**
     * 옵션 설명
     */
    private String description;

    /**
     * 옵션 이미지 URL
     */
    private String imageUrl;

    /**
     * SKU 코드
     */
    private String skuCode;

    /**
     * 재고 상태 (AVAILABLE, LOW_STOCK, OUT_OF_STOCK)
     */
    private String stockStatus;

    /**
     * 최저 재고 임계값
     */
    private Integer lowStockThreshold;

    /**
     * 옵션 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 옵션 수정일시
     */
    private LocalDateTime updatedAt;

    /**
     * 옵션 그룹 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionGroupInfo {
        private String groupName;
        private String displayName;
        private Boolean isRequired;
        private Boolean isMultipleChoice;
        private Integer maxSelections;
        private String groupType;
        private Integer sortOrder;
    }

    /**
     * 재고 상태 계산
     */
    public String getStockStatus() {
        if (stockQuantity == null || stockQuantity <= 0) {
            return "OUT_OF_STOCK";
        } else if (lowStockThreshold != null && stockQuantity <= lowStockThreshold) {
            return "LOW_STOCK";
        } else {
            return "AVAILABLE";
        }
    }

    /**
     * 판매 가능 여부 계산
     */
    public Boolean getIsAvailable() {
        return isActive != null && isActive && 
               stockQuantity != null && stockQuantity > 0;
    }

    /**
     * 총 가격 계산
     */
    public BigDecimal getTotalPrice() {
        if (basePrice == null) {
            return additionalPrice != null ? additionalPrice : BigDecimal.ZERO;
        }
        if (additionalPrice == null) {
            return basePrice;
        }
        return basePrice.add(additionalPrice);
    }
}