package com.dodam.product.dto.response;

import com.dodam.product.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 카테고리 응답 DTO
 * 
 * <p>카테고리 정보를 클라이언트에게 전달하기 위한 응답 객체입니다.</p>
 * 
 * @since 1.0.0
 */
@Schema(description = "카테고리 응답")
public class CategoryResponse {
    
    @Schema(description = "카테고리 ID", example = "1")
    private final Long categoryId;
    
    @Schema(description = "카테고리명", example = "전자제품")
    private final String categoryName;
    
    @Schema(description = "부모 카테고리 ID", example = "null")
    private final Long parentCategoryId;
    
    @Schema(description = "카테고리 경로", example = "/전자제품/")
    private final String categoryPath;
    
    @Schema(description = "표시 순서", example = "1")
    private final Integer displayOrder;
    
    @Schema(description = "활성화 여부", example = "true")
    private final Boolean isActive;
    
    @Schema(description = "카테고리별 상품 개수", example = "15")
    private final Long productCount;
    
    /**
     * 생성자
     */
    private CategoryResponse(Long categoryId, String categoryName, Long parentCategoryId,
                            String categoryPath, Integer displayOrder, Boolean isActive, Long productCount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.parentCategoryId = parentCategoryId;
        this.categoryPath = categoryPath;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
        this.productCount = productCount;
    }
    
    /**
     * Category 엔티티로부터 CategoryResponse를 생성합니다.
     * 
     * @param category Category 엔티티
     * @return CategoryResponse 객체
     */
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
            category.getCategoryId(),
            category.getCategoryName(),
            category.getParentCategoryId(),
            category.getCategoryPath(),
            category.getDisplayOrder(),
            category.getIsActive(),
            (long) category.getProducts().size()
        );
    }
    
    /**
     * Category 엔티티로부터 상품 개수가 포함된 CategoryResponse를 생성합니다.
     * 
     * @param category Category 엔티티
     * @param productCount 상품 개수
     * @return CategoryResponse 객체
     */
    public static CategoryResponse of(Category category, Long productCount) {
        return new CategoryResponse(
            category.getCategoryId(),
            category.getCategoryName(),
            category.getParentCategoryId(),
            category.getCategoryPath(),
            category.getDisplayOrder(),
            category.getIsActive(),
            productCount
        );
    }
    
    /**
     * 직접 정보를 지정하여 CategoryResponse를 생성합니다.
     */
    public static CategoryResponse of(Long categoryId, String categoryName, Long parentCategoryId,
                                     String categoryPath, Integer displayOrder, Boolean isActive, Long productCount) {
        return new CategoryResponse(categoryId, categoryName, parentCategoryId,
                                  categoryPath, displayOrder, isActive, productCount);
    }
    
    // === Getters ===
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public Long getParentCategoryId() {
        return parentCategoryId;
    }
    
    public String getCategoryPath() {
        return categoryPath;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public Long getProductCount() {
        return productCount;
    }
}