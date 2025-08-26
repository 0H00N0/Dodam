package com.dodam.product.service;

import com.dodam.product.dto.response.CategoryResponse;
import com.dodam.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 카테고리 서비스 인터페이스
 * 
 * <p>카테고리 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.</p>
 * 
 * @since 1.0.0
 */
public interface CategoryService {
    
    /**
     * 모든 활성화된 카테고리를 조회합니다.
     * 
     * @return 카테고리 목록
     */
    List<CategoryResponse> findAllActiveCategories();
    
    /**
     * 카테고리 ID로 카테고리를 조회합니다.
     * 
     * @param categoryId 카테고리 ID
     * @return 카테고리 정보
     */
    CategoryResponse findById(Long categoryId);
    
    /**
     * 루트 카테고리 목록을 조회합니다. (부모가 없는 카테고리)
     * 
     * @return 루트 카테고리 목록
     */
    List<CategoryResponse> findRootCategories();
    
    /**
     * 특정 부모 카테고리의 하위 카테고리를 조회합니다.
     * 
     * @param parentCategoryId 부모 카테고리 ID
     * @return 하위 카테고리 목록
     */
    List<CategoryResponse> findByParentCategoryId(Long parentCategoryId);
    
    /**
     * 카테고리별 상품을 조회합니다.
     * 
     * @param categoryId 카테고리 ID
     * @param pageable 페이징 정보
     * @return 상품 목록
     */
    Page<ProductResponse> findProductsByCategory(Long categoryId, Pageable pageable);
    
    /**
     * 카테고리가 존재하는지 확인합니다.
     * 
     * @param categoryId 카테고리 ID
     * @return 존재 여부
     */
    boolean existsById(Long categoryId);
    
    /**
     * 활성화된 카테고리인지 확인합니다.
     * 
     * @param categoryId 카테고리 ID
     * @return 활성화 여부
     */
    boolean isActiveCategory(Long categoryId);
}