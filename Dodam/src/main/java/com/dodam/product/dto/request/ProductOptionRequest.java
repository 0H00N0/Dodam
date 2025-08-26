package com.dodam.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * 상품 옵션 생성/수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionRequest {

    /**
     * 상품 ID
     */
    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;

    /**
     * 옵션 그룹 (예: "색상", "사이즈")
     */
    @NotBlank(message = "옵션 그룹은 필수입니다")
    private String optionGroup;

    /**
     * 옵션명 (예: "빨간색", "XL")
     */
    @NotBlank(message = "옵션명은 필수입니다")
    private String optionName;

    /**
     * 옵션 값 (예: "RED", "XL")
     */
    @NotBlank(message = "옵션 값은 필수입니다")
    private String optionValue;

    /**
     * 추가 가격 (기본 가격에 더해지는 가격)
     */
    @PositiveOrZero(message = "추가 가격은 0 이상이어야 합니다")
    @Builder.Default
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    /**
     * 재고 수량
     */
    @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다")
    @Builder.Default
    private Integer stockQuantity = 0;

    /**
     * 옵션 순서 (정렬용)
     */
    @PositiveOrZero(message = "순서는 0 이상이어야 합니다")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 옵션 활성화 여부
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 옵션 설명
     */
    private String description;

    /**
     * 옵션 이미지 URL
     */
    private String imageUrl;

    /**
     * SKU 코드 (Stock Keeping Unit)
     */
    private String skuCode;
}