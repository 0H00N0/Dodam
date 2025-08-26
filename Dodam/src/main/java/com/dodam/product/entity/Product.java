package com.dodam.product.entity;

import com.dodam.product.common.enums.ProductStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 상품 엔티티 (핵심 집계 루트)
 * 
 * <p>상품 도메인의 핵심 엔티티로, 상품과 관련된 모든 정보를 관리합니다.</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "PRODUCT", indexes = {
    @Index(name = "idx_product_name", columnList = "product_name"),
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_status", columnList = "status"),
    @Index(name = "idx_product_composite", columnList = "category_id, brand_id, status")
})
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;
    
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ProductStatus status;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductOption> options = new ArrayList<>();
    
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ProductDetail detail;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("imageOrder ASC")
    private List<ProductImage> images = new ArrayList<>();
    
    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY)
    private Inventory inventory;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 기본 생성자
     */
    protected Product() {}
    
    /**
     * 상품 생성자
     * 
     * @param productName 상품명
     * @param category 카테고리
     * @param price 가격
     */
    public Product(String productName, Category category, BigDecimal price) {
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.status = ProductStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // === 연관관계 편의 메서드 ===
    
    /**
     * 상품 옵션을 추가합니다.
     * 
     * @param option 추가할 상품 옵션
     */
    public void addOption(ProductOption option) {
        options.add(option);
        option.setProduct(this);
    }
    
    /**
     * 상품 이미지를 추가합니다.
     * 
     * @param image 추가할 상품 이미지
     */
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }
    
    /**
     * 상품 상세 정보를 설정합니다.
     * 
     * @param detail 상품 상세 정보
     */
    public void setDetail(ProductDetail detail) {
        this.detail = detail;
        if (detail != null) {
            detail.setProduct(this);
        }
    }
    
    // === 비즈니스 메서드 ===
    
    /**
     * 상품 상태를 변경합니다.
     * 
     * @param newStatus 변경할 상태
     * @throws IllegalStateException 허용되지 않는 상태 전이인 경우
     */
    public void changeStatus(ProductStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("상태를 %s에서 %s로 변경할 수 없습니다.", 
                this.status.getDescription(), newStatus.getDescription())
            );
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 상품 정보를 수정합니다.
     * 
     * @param productName 상품명
     * @param price 가격
     */
    public void updateInfo(String productName, BigDecimal price) {
        this.productName = productName;
        this.price = price;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 주문 가능한 상품인지 확인합니다.
     * 
     * @return 주문 가능 여부
     */
    public boolean isOrderable() {
        return status.isOrderable() && 
               inventory != null && 
               inventory.getAvailableQuantity() > 0;
    }
    
    /**
     * 선택된 옵션들의 총 추가 가격을 계산합니다.
     * 
     * @param selectedOptionIds 선택된 옵션 ID 목록
     * @return 총 추가 가격
     */
    public BigDecimal calculateOptionPrice(List<Long> selectedOptionIds) {
        return options.stream()
            .filter(option -> selectedOptionIds.contains(option.getOptionId()))
            .map(ProductOption::getAdditionalPrice)
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
        return price.add(optionPrice);
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public Brand getBrand() {
        return brand;
    }
    
    public void setBrand(Brand brand) {
        this.brand = brand;
    }
    
    public BigDecimal getPrice() {
        return price;
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
    
    public List<ProductOption> getOptions() {
        return new ArrayList<>(options);
    }
    
    public ProductDetail getDetail() {
        return detail;
    }
    
    public List<ProductImage> getImages() {
        return new ArrayList<>(images);
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}