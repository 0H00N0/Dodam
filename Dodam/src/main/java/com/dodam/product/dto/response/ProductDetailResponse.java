package com.dodam.product.dto.response;

import com.dodam.product.common.enums.ProductStatus;
import com.dodam.product.entity.Product;
import com.dodam.product.entity.ProductImage;
import com.dodam.product.entity.ProductOption;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 상세 응답 DTO
 * 
 * <p>상품의 상세 정보를 클라이언트에 응답할 때 사용하는 DTO입니다.
 * 옵션, 이미지, 상세 설명 등 모든 정보를 포함합니다.</p>
 * 
 * @since 1.0.0
 */
public class ProductDetailResponse {
    
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
    private String description;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private List<ProductOptionDto> options;
    private List<ProductImageDto> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 기본 생성자
     */
    public ProductDetailResponse() {
        this.options = new ArrayList<>();
        this.images = new ArrayList<>();
    }
    
    /**
     * Product 엔티티로부터 ProductDetailResponse를 생성합니다.
     * 
     * @param product 상품 엔티티
     * @return ProductDetailResponse 객체
     */
    public static ProductDetailResponse from(Product product) {
        ProductDetailResponse response = new ProductDetailResponse();
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
        response.description = product.getDetail() != null ? product.getDetail().getDescription() : null;
        
        // 재고 정보
        if (product.getInventory() != null) {
            response.quantity = product.getInventory().getQuantity();
            response.reservedQuantity = product.getInventory().getReservedQuantity();
            response.availableQuantity = product.getInventory().getAvailableQuantity();
        }
        
        // 옵션 정보
        response.options = product.getOptions().stream()
                .map(ProductOptionDto::from)
                .collect(Collectors.toList());
        
        // 이미지 정보
        response.images = product.getImages().stream()
                .map(ProductImageDto::from)
                .collect(Collectors.toList());
        
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
        return status != null && status.isOrderable() && 
               availableQuantity != null && availableQuantity > 0;
    }
    
    /**
     * 선택된 옵션들의 총 추가 가격을 계산합니다.
     * 
     * @param selectedOptionIds 선택된 옵션 ID 목록
     * @return 총 추가 가격
     */
    public BigDecimal calculateOptionPrice(List<Long> selectedOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return options.stream()
                .filter(option -> selectedOptionIds.contains(option.getOptionId()))
                .map(ProductOptionDto::getAdditionalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 최종 상품 가격을 계산합니다.
     * 
     * @param selectedOptionIds 선택된 옵션 ID 목록
     * @return 최종 가격
     */
    public BigDecimal calculateTotalPrice(List<Long> selectedOptionIds) {
        BigDecimal optionPrice = calculateOptionPrice(selectedOptionIds);
        return price != null ? price.add(optionPrice) : optionPrice;
    }
    
    // === Inner DTOs ===
    
    /**
     * 상품 옵션 DTO
     */
    public static class ProductOptionDto {
        private Long optionId;
        private String optionName;
        private String optionValue;
        private BigDecimal additionalPrice;
        private Integer stockQuantity;
        private Integer displayOrder;
        
        public static ProductOptionDto from(ProductOption option) {
            ProductOptionDto dto = new ProductOptionDto();
            dto.optionId = option.getOptionId();
            dto.optionName = option.getOptionName();
            dto.optionValue = option.getOptionValue();
            dto.additionalPrice = option.getAdditionalPrice();
            dto.stockQuantity = option.getStockQuantity();
            dto.displayOrder = option.getDisplayOrder();
            return dto;
        }
        
        // Getters and Setters
        public Long getOptionId() { return optionId; }
        public void setOptionId(Long optionId) { this.optionId = optionId; }
        public String getOptionName() { return optionName; }
        public void setOptionName(String optionName) { this.optionName = optionName; }
        public String getOptionValue() { return optionValue; }
        public void setOptionValue(String optionValue) { this.optionValue = optionValue; }
        public BigDecimal getAdditionalPrice() { return additionalPrice; }
        public void setAdditionalPrice(BigDecimal additionalPrice) { this.additionalPrice = additionalPrice; }
        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }
    
    /**
     * 상품 이미지 DTO
     */
    public static class ProductImageDto {
        private Long imageId;
        private String imageUrl;
        private String altText;
        private Integer imageOrder;
        
        public static ProductImageDto from(ProductImage image) {
            ProductImageDto dto = new ProductImageDto();
            dto.imageId = image.getImageId();
            dto.imageUrl = image.getImageUrl();
            dto.altText = image.getAltText();
            dto.imageOrder = image.getImageOrder();
            return dto;
        }
        
        // Getters and Setters
        public Long getImageId() { return imageId; }
        public void setImageId(Long imageId) { this.imageId = imageId; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getAltText() { return altText; }
        public void setAltText(String altText) { this.altText = altText; }
        public Integer getImageOrder() { return imageOrder; }
        public void setImageOrder(Integer imageOrder) { this.imageOrder = imageOrder; }
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Integer getReservedQuantity() {
        return reservedQuantity;
    }
    
    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
    
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
    
    public List<ProductOptionDto> getOptions() {
        return options;
    }
    
    public void setOptions(List<ProductOptionDto> options) {
        this.options = options;
    }
    
    public List<ProductImageDto> getImages() {
        return images;
    }
    
    public void setImages(List<ProductImageDto> images) {
        this.images = images;
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