package com.dodam.product.dto.request;

import com.dodam.product.entity.Category;
import com.dodam.product.entity.Product;
import com.dodam.product.entity.Product.ProductStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 요청 DTO
 * 상품 생성, 수정, 검색 요청 시 사용됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    /**
     * 상품명 (필수값)
     * 1자 이상 200자 이하로 입력해야 합니다.
     */
    @NotBlank(message = "상품명은 필수입니다.")
    @Size(min = 1, max = 200, message = "상품명은 1자 이상 200자 이하로 입력해주세요.")
    private String productName;

    /**
     * 상품 이미지 파일명 (선택값)
     * 255자 이하로 입력 가능합니다.
     */
    @Size(max = 255, message = "이미지 파일명은 255자 이하로 입력해주세요.")
    private String imageName;

    /**
     * 상품 가격 (필수값)
     * 0원 이상 9,999,999.99원 이하로 입력해야 합니다.
     */
    @NotNull(message = "상품 가격은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "상품 가격은 0원보다 커야 합니다.")
    @DecimalMax(value = "9999999.99", message = "상품 가격은 9,999,999.99원 이하로 입력해주세요.")
    @Digits(integer = 7, fraction = 2, message = "상품 가격은 소수점 2자리까지 입력 가능합니다.")
    private BigDecimal price;

    /**
     * 상품 설명 (선택값)
     * 2000자 이하로 입력 가능합니다.
     */
    @Size(max = 2000, message = "상품 설명은 2000자 이하로 입력해주세요.")
    private String description;

    /**
     * 재고 수량 (필수값)
     * 0개 이상 999,999개 이하로 입력해야 합니다.
     */
    @NotNull(message = "재고 수량은 필수입니다.")
    @Min(value = 0, message = "재고 수량은 0개 이상이어야 합니다.")
    @Max(value = 999999, message = "재고 수량은 999,999개 이하로 입력해주세요.")
    private Integer stockQuantity;

    /**
     * 판매 상태
     * ACTIVE, INACTIVE, OUT_OF_STOCK 중 하나를 입력해야 합니다.
     */
    private ProductStatus status;

    /**
     * 카테고리 ID (필수값)
     * 1 이상의 값을 입력해야 합니다.
     */
    @NotNull(message = "카테고리는 필수입니다.")
    @Min(value = 1, message = "유효한 카테고리를 선택해주세요.")
    private Long categoryId;

    /**
     * 생성일시 (수정 시에만 사용)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    /**
     * 수정일시 (수정 시에만 사용)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    // 검색용 필드들 (선택값)

    /**
     * 검색 키워드 (상품명, 설명에서 검색)
     */
    private String keyword;

    /**
     * 최소 가격 (검색용)
     */
    @DecimalMin(value = "0.0", message = "최소 가격은 0원 이상이어야 합니다.")
    private BigDecimal minPrice;

    /**
     * 최대 가격 (검색용)
     */
    @DecimalMin(value = "0.0", message = "최대 가격은 0원 이상이어야 합니다.")
    private BigDecimal maxPrice;

    /**
     * 카테고리 ID 목록 (검색용)
     */
    private List<Long> categoryIds;

    /**
     * 상품 상태 목록 (검색용)
     */
    private List<ProductStatus> statuses;

    /**
     * 재고 있는 상품만 조회 여부 (검색용)
     */
    private Boolean inStockOnly;

    /**
     * 정렬 기준 (검색용)
     * name, price, createdAt, stockQuantity 등
     */
    private String sortBy;

    /**
     * 정렬 방향 (검색용)
     * ASC, DESC
     */
    private String sortDirection;

    /**
     * 페이지 번호 (검색용, 0부터 시작)
     */
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private Integer page;

    /**
     * 페이지 크기 (검색용)
     */
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 100 이하로 설정해주세요.")
    private Integer size;

    /**
     * RequestDto를 Entity로 변환하는 메소드 (생성용)
     * 
     * @param category 카테고리 엔티티
     * @return Product 엔티티 객체
     */
    public Product toEntity(Category category) {
        return Product.builder()
                .productName(this.productName)
                .imageName(this.imageName)
                .price(this.price)
                .description(this.description)
                .stockQuantity(this.stockQuantity != null ? this.stockQuantity : 0)
                .status(this.status != null ? this.status : ProductStatus.ACTIVE)
                .category(category)
                .build();
    }

    /**
     * RequestDto를 Entity로 변환하는 메소드 (수정용)
     * 
     * @param productId 상품 ID
     * @param category 카테고리 엔티티
     * @return Product 엔티티 객체
     */
    public Product toEntity(Long productId, Category category) {
        return Product.builder()
                .productId(productId)
                .productName(this.productName)
                .imageName(this.imageName)
                .price(this.price)
                .description(this.description)
                .stockQuantity(this.stockQuantity != null ? this.stockQuantity : 0)
                .status(this.status != null ? this.status : ProductStatus.ACTIVE)
                .category(category)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * 기존 Entity를 RequestDto로 업데이트하는 메소드
     * 
     * @param product 업데이트할 상품 엔티티
     * @param category 새로운 카테고리 엔티티 (변경 시)
     */
    public void updateEntity(Product product, Category category) {
        if (this.productName != null) {
            product.setProductName(this.productName);
        }
        if (this.imageName != null) {
            product.setImageName(this.imageName);
        }
        if (this.price != null) {
            product.setPrice(this.price);
        }
        if (this.description != null) {
            product.setDescription(this.description);
        }
        if (this.stockQuantity != null) {
            product.setStockQuantity(this.stockQuantity);
        }
        if (this.status != null) {
            product.setStatus(this.status);
        }
        if (category != null) {
            product.setCategory(category);
        }
    }

    /**
     * 유효성 검사 통과 여부 확인
     * 
     * @return 유효성 검사 통과 여부
     */
    public boolean isValid() {
        return productName != null && 
               !productName.trim().isEmpty() && 
               productName.length() <= 200 &&
               price != null && 
               price.compareTo(BigDecimal.ZERO) > 0 &&
               stockQuantity != null && 
               stockQuantity >= 0 &&
               categoryId != null && 
               categoryId > 0;
    }

    /**
     * 검색 조건이 있는지 확인
     * 
     * @return 검색 조건 존재 여부
     */
    public boolean hasSearchConditions() {
        return keyword != null || 
               minPrice != null || 
               maxPrice != null || 
               (categoryIds != null && !categoryIds.isEmpty()) ||
               (statuses != null && !statuses.isEmpty()) ||
               inStockOnly != null;
    }

    /**
     * 가격 범위 유효성 검사
     * 
     * @return 가격 범위 유효성
     */
    public boolean isValidPriceRange() {
        if (minPrice != null && maxPrice != null) {
            return minPrice.compareTo(maxPrice) <= 0;
        }
        return true;
    }

    /**
     * 상품 정보 정규화 (앞뒤 공백 제거, 기본값 설정)
     * 
     * @return 정규화된 RequestDto
     */
    public ProductRequestDto normalize() {
        if (this.productName != null) {
            this.productName = this.productName.trim();
        }
        if (this.description != null && this.description.trim().isEmpty()) {
            this.description = null;
        }
        if (this.keyword != null) {
            this.keyword = this.keyword.trim();
            if (this.keyword.isEmpty()) {
                this.keyword = null;
            }
        }
        if (this.status == null) {
            this.status = ProductStatus.ACTIVE;
        }
        if (this.stockQuantity == null) {
            this.stockQuantity = 0;
        }
        return this;
    }

    /**
     * 생성용 RequestDto 생성 팩토리 메소드
     * 
     * @param productName 상품명
     * @param price 가격
     * @param stockQuantity 재고 수량
     * @param categoryId 카테고리 ID
     * @return ProductRequestDto 객체
     */
    public static ProductRequestDto createRequest(String productName, BigDecimal price, 
                                                 Integer stockQuantity, Long categoryId) {
        return ProductRequestDto.builder()
                .productName(productName)
                .price(price)
                .stockQuantity(stockQuantity)
                .categoryId(categoryId)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    /**
     * 검색용 RequestDto 생성 팩토리 메소드
     * 
     * @param keyword 검색 키워드
     * @param categoryId 카테고리 ID
     * @return ProductRequestDto 객체
     */
    public static ProductRequestDto searchRequest(String keyword, Long categoryId) {
        return ProductRequestDto.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .inStockOnly(true)
                .page(0)
                .size(20)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();
    }

    /**
     * Entity로부터 RequestDto 생성 (수정 시 기존 값 로드용)
     * 
     * @param product 상품 엔티티
     * @return ProductRequestDto 객체
     */
    public static ProductRequestDto fromEntity(Product product) {
        if (product == null) {
            return null;
        }

        return ProductRequestDto.builder()
                .productName(product.getProductName())
                .imageName(product.getImageName())
                .price(product.getPrice())
                .description(product.getDescription())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Override
    public String toString() {
        return String.format("ProductRequestDto(productName=%s, price=%s, stockQuantity=%d, categoryId=%d)", 
                           productName, price, stockQuantity, categoryId);
    }
}