package com.dodam.product.entity;

import com.dodam.product.common.enums.OptionType;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 상품 옵션 엔티티
 * 
 * <p>상품의 다양한 옵션(색상, 사이즈 등)을 관리합니다.</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "PRODUCT_OPTION", indexes = {
    @Index(name = "idx_product_option_product", columnList = "product_id"),
    @Index(name = "idx_product_option_type", columnList = "option_type")
})
public class ProductOption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", length = 20, nullable = false)
    private OptionType optionType;
    
    @Column(name = "option_name", nullable = false, length = 50)
    private String optionName;
    
    @Column(name = "option_value", nullable = false, length = 100)
    private String optionValue;
    
    @Column(name = "additional_price", precision = 15, scale = 2)
    private BigDecimal additionalPrice;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;
    
    /**
     * 기본 생성자
     */
    protected ProductOption() {}
    
    /**
     * 상품 옵션 생성자
     * 
     * @param optionType 옵션 타입
     * @param optionName 옵션명
     * @param optionValue 옵션값
     * @param displayOrder 표시 순서
     */
    public ProductOption(OptionType optionType, String optionName, String optionValue, Integer displayOrder) {
        this.optionType = optionType;
        this.optionName = optionName;
        this.optionValue = optionValue;
        this.displayOrder = displayOrder;
        this.additionalPrice = BigDecimal.ZERO;
        this.stockQuantity = 0;
        this.isAvailable = true;
    }
    
    /**
     * 상품 옵션 생성자 (추가 가격 포함)
     * 
     * @param optionType 옵션 타입
     * @param optionName 옵션명
     * @param optionValue 옵션값
     * @param additionalPrice 추가 가격
     * @param displayOrder 표시 순서
     */
    public ProductOption(OptionType optionType, String optionName, String optionValue, 
                        BigDecimal additionalPrice, Integer displayOrder) {
        this(optionType, optionName, optionValue, displayOrder);
        this.additionalPrice = additionalPrice != null ? additionalPrice : BigDecimal.ZERO;
    }
    
    // === 비즈니스 메서드 ===
    
    /**
     * 옵션을 비활성화합니다.
     */
    public void disable() {
        this.isAvailable = false;
    }
    
    /**
     * 옵션을 활성화합니다.
     */
    public void enable() {
        this.isAvailable = true;
    }
    
    /**
     * 옵션 정보를 수정합니다.
     * 
     * @param optionName 옵션명
     * @param optionValue 옵션값
     * @param additionalPrice 추가 가격
     */
    public void updateInfo(String optionName, String optionValue, BigDecimal additionalPrice) {
        this.optionName = optionName;
        this.optionValue = optionValue;
        this.additionalPrice = additionalPrice != null ? additionalPrice : BigDecimal.ZERO;
    }
    
    /**
     * 재고 수량을 업데이트합니다.
     * 
     * @param quantity 새로운 재고 수량
     */
    public void updateStock(Integer quantity) {
        this.stockQuantity = quantity;
    }
    
    /**
     * 선택 가능한 옵션인지 확인합니다.
     * 
     * @return 선택 가능 여부
     */
    public boolean isSelectable() {
        return isAvailable && (stockQuantity == null || stockQuantity > 0);
    }
    
    // === Getters and Setters ===
    
    public Long getOptionId() {
        return optionId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public OptionType getOptionType() {
        return optionType;
    }
    
    public String getOptionName() {
        return optionName;
    }
    
    public String getOptionValue() {
        return optionValue;
    }
    
    public BigDecimal getAdditionalPrice() {
        return additionalPrice;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
}