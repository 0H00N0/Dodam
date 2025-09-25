package com.dodam.admin.dto;

import com.dodam.product.entity.CategoryEntity;
import lombok.Getter;

@Getter
public class CategoryResponseDTO {
    private Long categoryId;
    private String categoryName;

    public CategoryResponseDTO(CategoryEntity category) {
        this.categoryId = category.getCatenum();
        this.categoryName = category.getCatename();
    }
}