package com.dodam.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 상품 정보를 관리하는 Entity
 * 판매되는 상품들의 기본 정보를 저장합니다.
 */
@Entity
@Table(name = "product", indexes = {
    @Index(name = "idx_product_name", columnList = "productName"),
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_price", columnList = "price")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"category", "reviews"}) // 순환 참조 방지
public class Product {

    /**
     * 상품 고유 번호 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    /**
     * 상품명 (필수값)
     */
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    /**
     * 상품 이미지 파일명
     */
    @Column(name = "image_name", length = 255)
    private String imageName;

    /**
     * 상품 가격
     */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 상품 설명
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * 재고 수량
     */
    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    /**
     * 판매 상태 (ACTIVE: 판매중, INACTIVE: 판매중지, OUT_OF_STOCK: 품절)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    /**
     * 생성일시
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 삭제일시 (소프트 삭제용)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 카테고리 (외래키)
     * 지연 로딩으로 성능 최적화
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * 이 상품에 대한 리뷰들
     * 지연 로딩으로 성능 최적화
     */
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    /**
     * 상품 판매 상태 열거형
     */
    public enum ProductStatus {
        ACTIVE("판매중"),
        INACTIVE("판매중지"),
        OUT_OF_STOCK("품절");

        private final String description;

        ProductStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 구매 가능 여부 확인
         * @return 구매 가능 여부
         */
        public boolean isAvailable() {
            return this == ACTIVE;
        }
    }

    /**
     * 상품이 삭제되었는지 확인
     * @return 삭제 여부
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 상품 소프트 삭제
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.status = ProductStatus.INACTIVE;
    }

    /**
     * 상품 복구
     */
    public void restore() {
        this.deletedAt = null;
        this.status = ProductStatus.ACTIVE;
    }

    /**
     * 재고 감소
     * @param quantity 감소할 수량
     * @throws IllegalArgumentException 재고 부족 시
     */
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stockQuantity -= quantity;
        
        if (this.stockQuantity == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        }
    }

    /**
     * 재고 증가
     * @param quantity 증가할 수량
     */
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
        
        if (this.status == ProductStatus.OUT_OF_STOCK && this.stockQuantity > 0) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    /**
     * 평균 평점 계산
     * @return 평균 평점 (리뷰가 없는 경우 0.0)
     */
    public double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        
        return reviews.stream()
            .filter(review -> !review.isDeleted())
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
    }

    /**
     * 리뷰 개수 조회
     * @return 리뷰 개수 (삭제되지 않은 리뷰만)
     */
    public int getReviewCount() {
        if (reviews == null) {
            return 0;
        }
        
        return (int) reviews.stream()
            .filter(review -> !review.isDeleted())
            .count();
    }
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRAND_ID") // PRODUCT.BRAND_ID FK
    private Brand brand;
}