package com.dodam.product.entity;

import com.dodam.product.common.enums.OptionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 옵션 그룹 엔티티
 * 
 * <p>상품의 옵션들을 그룹화하여 관리합니다. (예: 색상, 크기, 용량 등)</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "OPTION_GROUP", indexes = {
    @Index(name = "idx_option_group_product", columnList = "product_id")
})
public class OptionGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_group_id")
    private Long optionGroupId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false, length = 50)
    private OptionType optionType;
    
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> productOptions;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 기본 생성자
     */
    public OptionGroup() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 옵션 그룹 생성자
     * 
     * @param product 상품
     * @param groupName 그룹명
     * @param optionType 옵션 유형
     * @param isRequired 필수 여부
     */
    public OptionGroup(Product product, String groupName, OptionType optionType, Boolean isRequired) {
        this();
        this.product = product;
        this.groupName = groupName;
        this.optionType = optionType;
        this.isRequired = isRequired != null ? isRequired : false;
    }
    
    // === 비즈니스 메서드 ===
    
    /**
     * 옵션 그룹 정보를 업데이트합니다.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 생성 시간을 설정합니다.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 옵션 개수를 반환합니다.
     * 
     * @return 옵션 개수
     */
    public int getOptionCount() {
        return productOptions != null ? productOptions.size() : 0;
    }
    
    /**
     * 활성화된 옵션 개수를 반환합니다.
     * 
     * @return 활성화된 옵션 개수
     */
    public long getActiveOptionCount() {
        if (productOptions == null) {
            return 0;
        }
        return productOptions.stream()
                .filter(option -> option.getIsAvailable() != null && option.getIsAvailable())
                .count();
    }
    
    // === Getters and Setters ===
    
    public Long getOptionGroupId() {
        return optionGroupId;
    }
    
    public void setOptionGroupId(Long optionGroupId) {
        this.optionGroupId = optionGroupId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    public OptionType getOptionType() {
        return optionType;
    }
    
    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }
    
    public Boolean getIsRequired() {
        return isRequired;
    }
    
    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public List<ProductOption> getProductOptions() {
        return productOptions;
    }
    
    public void setProductOptions(List<ProductOption> productOptions) {
        this.productOptions = productOptions;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}