package com.dodam.admin.service;

import com.dodam.admin.dto.AdminCategoryDto;
import com.dodam.product.entity.Category;
import com.dodam.product.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public AdminCategoryDto.Response createCategory(AdminCategoryDto.CreateRequest requestDto) {
        categoryRepository.findByCategoryName(requestDto.getCategoryName()).ifPresent(c -> {
            throw new IllegalArgumentException("Category with this name already exists.");
        });
        Category category = requestDto.toEntity();
        Category savedCategory = categoryRepository.save(category);
        return AdminCategoryDto.Response.fromEntity(savedCategory);
    }

    public List<AdminCategoryDto.Response> getAllCategories() {
        return categoryRepository.findAll().stream()
                .filter(c -> !c.isDeleted())
                .map(AdminCategoryDto.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public AdminCategoryDto.Response getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        return AdminCategoryDto.Response.fromEntity(category);
    }

    @Transactional
    public AdminCategoryDto.Response updateCategory(Long categoryId, AdminCategoryDto.UpdateRequest requestDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

        categoryRepository.findByCategoryName(requestDto.getCategoryName()).ifPresent(c -> {
            if (!c.getCategoryId().equals(categoryId)) {
                throw new IllegalArgumentException("Another category with this name already exists.");
            }
        });

        category.setCategoryName(requestDto.getCategoryName());
        category.setDescription(requestDto.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return AdminCategoryDto.Response.fromEntity(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        category.delete();
        categoryRepository.save(category);
    }
}
