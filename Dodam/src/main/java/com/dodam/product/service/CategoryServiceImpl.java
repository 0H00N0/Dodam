package com.dodam.product.service;

import com.dodam.product.dto.response.CategoryResponse;
import com.dodam.product.dto.response.ProductResponse;
import com.dodam.product.entity.Category;
import com.dodam.product.repository.CategoryRepository;
import com.dodam.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 카테고리 서비스 구현체
 * 
 * <p>카테고리 관련 비즈니스 로직을 구현합니다.</p>
 * 
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    
    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductService productService) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }
    
    @Override
    public List<CategoryResponse> findAllActiveCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        
        return categories.stream()
            .map(CategoryResponse::from)
            .collect(Collectors.toList());
    }
    
    @Override
    public CategoryResponse findById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. ID: " + categoryId));
        
        // 해당 카테고리의 상품 개수 조회
        long productCount = productService.countByCategory(categoryId, null);
        
        return CategoryResponse.of(category, productCount);
    }
    
    @Override
    public List<CategoryResponse> findRootCategories() {
        List<Category> rootCategories = categoryRepository.findByParentCategoryIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
        
        return rootCategories.stream()
            .map(category -> {
                long productCount = productService.countByCategory(category.getCategoryId(), null);
                return CategoryResponse.of(category, productCount);
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryResponse> findByParentCategoryId(Long parentCategoryId) {
        List<Category> subCategories = categoryRepository.findByParentCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(parentCategoryId);
        
        return subCategories.stream()
            .map(category -> {
                long productCount = productService.countByCategory(category.getCategoryId(), null);
                return CategoryResponse.of(category, productCount);
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<ProductResponse> findProductsByCategory(Long categoryId, Pageable pageable) {
        // 카테고리 존재 여부 확인
        if (!existsById(categoryId)) {
            throw new IllegalArgumentException("카테고리를 찾을 수 없습니다. ID: " + categoryId);
        }
        
        return productService.findByCategory(categoryId, pageable);
    }
    
    @Override
    public boolean existsById(Long categoryId) {
        return categoryRepository.existsById(categoryId);
    }
    
    @Override
    public boolean isActiveCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .map(Category::getIsActive)
            .orElse(false);
    }
}