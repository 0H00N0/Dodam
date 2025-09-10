package com.dodam.admin.dto;

import com.dodam.product.entity.Brand;
import com.dodam.product.entity.Category;
import com.dodam.product.entity.Product;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminProductDto {

    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        private String productName;
        private BigDecimal price;
        private String description;
        private Integer stockQuantity;
        private Long categoryId;
        private Long brandId;
        private Product.ProductStatus status;

        public Product toEntity(Category category, Brand brand) {
            return Product.builder()
                    .productName(this.productName)
                    .price(this.price)
                    .description(this.description)
                    .stockQuantity(this.stockQuantity)
                    .category(category)
                    .brand(brand)
                    .status(this.status)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String productName;
        private BigDecimal price;
        private String description;
        private Integer stockQuantity;
        private Long categoryId;
        private Long brandId;
        private Product.ProductStatus status;
    }

    @Getter
    @Builder
    public static class Response {
        private Long productId;
        private String productName;
        private BigDecimal price;
        private String description;
        private Integer stockQuantity;
        private Product.ProductStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String categoryName;
        private String brandName;

        public static Response fromEntity(Product product) {
            return Response.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .description(product.getDescription())
                    .stockQuantity(product.getStockQuantity())
                    .status(product.getStatus())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                    .brandName(product.getBrand() != null ? product.getBrand().getBrandName() : null)
                    .build();
        }
    }
}
