package com.dodam.product.dto.request;

import com.dodam.product.common.validator.ValidSku;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 상품 생성 요청 DTO
 * 
 * <p>새로운 상품을 생성할 때 클라이언트로부터 받는 데이터를 담는 DTO입니다.</p>
 * 
 * @since 1.0.0
 */
public class ProductCreateRequest {
    
    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 100, message = "상품명은 100자를 초과할 수 없습니다")
    private String productName;
    
    @NotNull(message = "카테고리는 필수입니다")
    private Long categoryId;
    
    private Long brandId;
    
    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0", message = "가격은 0원 이상이어야 합니다")
    private BigDecimal price;
    
    @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다")
    private String imageUrl;
    
    @Size(max = 2000, message = "상품 설명은 2000자를 초과할 수 없습니다")
    private String description;
    
    @ValidSku
    private String sku;
    
    /**
     * 기본 생성자
     */
    public ProductCreateRequest() {}
    
    /**
     * 전체 생성자
     * 
     * @param productName 상품명
     * @param categoryId 카테고리 ID
     * @param brandId 브랜드 ID
     * @param price 가격
     * @param imageUrl 이미지 URL
     * @param description 상품 설명
     * @param sku SKU 코드
     */
    public ProductCreateRequest(String productName, Long categoryId, Long brandId, 
                               BigDecimal price, String imageUrl, String description, String sku) {
        this.productName = productName;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.sku = sku;
    }
    
    // === Getters and Setters ===
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public Long getBrandId() {
        return brandId;
    }
    
    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    @Override
    public String toString() {
        return "ProductCreateRequest{" +
                "productName='" + productName + '\'' +
                ", categoryId=" + categoryId +
                ", brandId=" + brandId +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", description='" + description + '\'' +
                ", sku='" + sku + '\'' +
                '}';
    }
}