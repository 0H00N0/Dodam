package com.dodam.product.dto.response;

import com.dodam.product.common.enums.ProductStatus;
import com.dodam.product.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 응답 DTO
 * 
 * <p>상품 목록이나 간단한 상품 정보를 클라이언트에 응답할 때 사용하는 DTO입니다.</p>
 * 
 * @since 1.0.0
 */
public class ProductResponse {
    
    private Long productId;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private BigDecimal price;
    private String imageUrl;
    private ProductStatus status;
    private String statusDescription;
    private Integer availableQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 기본 생성자
     */
    public ProductResponse() {}
    
    /**
     * Product 엔티티로부터 ProductResponse를 생성합니다.
     * 
     * @param product 상품 엔티티
     * @return ProductResponse 객체
     */
    public static ProductResponse from(Product product) {
        ProductResponse response = new ProductResponse();
        response.productId = product.getProductId();
        response.productName = product.getProductName();
        response.categoryId = product.getCategory() != null ? product.getCategory().getCategoryId() : null;
        response.categoryName = product.getCategory() != null ? product.getCategory().getCategoryName() : null;
        response.brandId = product.getBrand() != null ? product.getBrand().getBrandId() : null;
        response.brandName = product.getBrand() != null ? product.getBrand().getBrandName() : null;
        response.price = product.getPrice();
        response.imageUrl = product.getImageUrl();
        response.status = product.getStatus();
        response.statusDescription = product.getStatus().getDescription();
        response.availableQuantity = product.getInventory() != null ? product.getInventory().getAvailableQuantity() : 0;
        response.createdAt = product.getCreatedAt();
        response.updatedAt = product.getUpdatedAt();
        return response;
    }
    
    /**
     * 주문 가능한 상품인지 확인합니다.
     * 
     * @return 주문 가능 여부
     */
    public boolean isOrderable() {
        return status != null && status.isOrderable() && availableQuantity != null && availableQuantity > 0;
    }
    
    // === Getters and Setters ===
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
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
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Long getBrandId() {
        return brandId;
    }
    
    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }
    
    public String getBrandName() {
        return brandName;
    }
    
    public void setBrandName(String brandName) {
        this.brandName = brandName;
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
    
    public ProductStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProductStatus status) {
        this.status = status;
    }
    
    public String getStatusDescription() {
        return statusDescription;
    }
    
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
    
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
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
    
    @Override
    public String toString() {
        return "ProductResponse{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", brandId=" + brandId +
                ", brandName='" + brandName + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", status=" + status +
                ", statusDescription='" + statusDescription + '\'' +
                ", availableQuantity=" + availableQuantity +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}