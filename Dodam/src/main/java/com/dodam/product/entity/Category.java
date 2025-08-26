package com.dodam.product.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 카테고리 엔티티
 * 
 * <p>상품을 분류하기 위한 카테고리 정보를 관리합니다.</p>
 * <p>계층형 구조를 지원하여 대분류/중분류/소분류 등을 표현할 수 있습니다.</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "CATEGORY", indexes = {
    @Index(name = "idx_category_parent", columnList = "parent_category_id"),
    @Index(name = "idx_category_path", columnList = "category_path")
})
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;
    
    @Column(name = "parent_category_id")
    private Long parentCategoryId;
    
    @Column(name = "category_path", length = 100)
    private String categoryPath;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
    
    /**
     * 기본 생성자
     */
    protected Category() {}
    
    /**
     * 카테고리 생성자
     * 
     * @param categoryName 카테고리명
     * @param parentCategoryId 부모 카테고리 ID
     * @param displayOrder 표시 순서
     */
    public Category(String categoryName, Long parentCategoryId, Integer displayOrder) {
        this.categoryName = categoryName;
        this.parentCategoryId = parentCategoryId;
        this.displayOrder = displayOrder;
        this.isActive = true;
        this.categoryPath = generateCategoryPath();
    }
    
    /**
     * 루트 카테고리 생성자
     * 
     * @param categoryName 카테고리명
     * @param displayOrder 표시 순서
     */
    public Category(String categoryName, Integer displayOrder) {
        this(categoryName, null, displayOrder);
    }
    
    // === 비즈니스 메서드 ===
    
    /**
     * 카테고리 경로를 생성합니다.
     * 
     * @return 카테고리 경로 (예: "/전자제품/스마트폰/")
     */
    private String generateCategoryPath() {
        if (parentCategoryId == null) {
            return "/" + categoryName + "/";
        }
        // 실제 구현에서는 부모 카테고리 조회 로직이 필요
        return "/" + categoryName + "/";
    }
    
    /**
     * 루트 카테고리인지 확인합니다.
     * 
     * @return 루트 카테고리 여부
     */
    public boolean isRootCategory() {
        return parentCategoryId == null;
    }
    
    /**
     * 카테고리를 비활성화합니다.
     */
    public void deactivate() {
        this.isActive = false;
    }
    
    /**
     * 카테고리를 활성화합니다.
     */
    public void activate() {
        this.isActive = true;
    }
    
    /**
     * 카테고리 정보를 수정합니다.
     * 
     * @param categoryName 카테고리명
     * @param displayOrder 표시 순서
     */
    public void updateInfo(String categoryName, Integer displayOrder) {
        this.categoryName = categoryName;
        this.displayOrder = displayOrder;
        this.categoryPath = generateCategoryPath();
    }
    
    // === Getters and Setters ===
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public Long getParentCategoryId() {
        return parentCategoryId;
    }
    
    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
        this.categoryPath = generateCategoryPath();
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
    
    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }
}