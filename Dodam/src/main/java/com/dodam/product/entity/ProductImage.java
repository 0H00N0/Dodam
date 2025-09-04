package com.dodam.product.entity;

import com.dodam.product.common.enums.ImageType;
import jakarta.persistence.*;

/**
 * 상품 이미지 엔티티
 * 
 * <p>상품의 이미지 정보를 관리합니다.</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "PRODUCT_IMAGE", indexes = {
    @Index(name = "idx_product_image_product", columnList = "product_id"),
    @Index(name = "idx_product_image_type", columnList = "image_type")
})
public class ProductImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", length = 20, nullable = false)
    private ImageType imageType;
    
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
    
    @Column(name = "alt_text", length = 200)
    private String altText;
    
    @Column(name = "image_order", nullable = false)
    private Integer imageOrder;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "width")
    private Integer width;
    
    @Column(name = "height")
    private Integer height;
    
    /**
     * 기본 생성자
     */
    protected ProductImage() {}
    
    /**
     * 상품 이미지 생성자
     * 
     * @param imageType 이미지 타입
     * @param imageUrl 이미지 URL
     * @param imageOrder 표시 순서
     */
    public ProductImage(ImageType imageType, String imageUrl, Integer imageOrder) {
        this.imageType = imageType;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
        this.isActive = true;
    }
    
    /**
     * 상품 이미지 생성자 (대체 텍스트 포함)
     * 
     * @param imageType 이미지 타입
     * @param imageUrl 이미지 URL
     * @param altText 대체 텍스트
     * @param imageOrder 표시 순서
     */
    public ProductImage(ImageType imageType, String imageUrl, String altText, Integer imageOrder) {
        this(imageType, imageUrl, imageOrder);
        this.altText = altText;
    }
    
    // === 비즈니스 메서드 ===
    
    /**
     * 이미지를 비활성화합니다.
     */
    public void deactivate() {
        this.isActive = false;
    }
    
    /**
     * 이미지를 활성화합니다.
     */
    public void activate() {
        this.isActive = true;
    }
    
    /**
     * 이미지 정보를 수정합니다.
     * 
     * @param imageUrl 이미지 URL
     * @param altText 대체 텍스트
     */
    public void updateInfo(String imageUrl, String altText) {
        this.imageUrl = imageUrl;
        this.altText = altText;
    }
    
    /**
     * 이미지 순서를 변경합니다.
     * 
     * @param newOrder 새로운 순서
     */
    public void changeOrder(Integer newOrder) {
        this.imageOrder = newOrder;
    }
    
    /**
     * 이미지 메타데이터를 업데이트합니다.
     * 
     * @param fileSize 파일 크기 (bytes)
     * @param width 이미지 너비
     * @param height 이미지 높이
     */
    public void updateMetadata(Long fileSize, Integer width, Integer height) {
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
    }
    
    /**
     * 썸네일 이미지인지 확인합니다.
     * 
     * @return 썸네일 이미지 여부
     */
    public boolean isThumbnail() {
        return imageType == ImageType.THUMBNAIL;
    }
    
    /**
     * 대표 이미지 후보인지 확인합니다.
     * 
     * @return 대표 이미지 후보 여부
     */
    public boolean isPrimaryCandidate() {
        return isActive && (imageType == ImageType.THUMBNAIL || imageType == ImageType.DETAIL);
    }
    
    // === Getters and Setters ===
    
    public Long getImageId() {
        return imageId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public ImageType getImageType() {
        return imageType;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public String getAltText() {
        return altText;
    }
    
    public Integer getImageOrder() {
        return imageOrder;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public Integer getWidth() {
        return width;
    }
    
    public Integer getHeight() {
        return height;
    }
}