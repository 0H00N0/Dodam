package com.dodam.admin.dto;

import com.dodam.product.entity.Category;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class AdminCategoryDto {

    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        private String categoryName;
        private String description;

        public Category toEntity() {
            return Category.builder()
                    .categoryName(this.categoryName)
                    .description(this.description)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String categoryName;
        private String description;
    }

    @Getter
    @Builder
    public static class Response {
        private Long categoryId;
        private String categoryName;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(Category category) {
            return Response.builder()
                    .categoryId(category.getCategoryId())
                    .categoryName(category.getCategoryName())
                    .description(category.getDescription())
                    .createdAt(category.getCreatedAt())
                    .updatedAt(category.getUpdatedAt())
                    .build();
        }
    }
}
