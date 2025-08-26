package com.dodam.product.repository;

import com.dodam.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * 카테고리 Repository 인터페이스
 * 
 * <p>카테고리 엔티티의 데이터베이스 액세스를 담당합니다.</p>
 * <p>계층형 카테고리 구조를 지원하는 메서드들을 포함합니다.</p>
 * 
 * @since 1.0.0
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // === 기본 조회 메서드 ===
    
    /**
     * 활성화된 카테고리만 조회 (표시 순서대로 정렬)
     * 
     * @return 활성 카테고리 목록
     */
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    /**
     * 카테고리명으로 조회
     * 
     * @param categoryName 카테고리명
     * @return 카테고리 Optional
     */
    Optional<Category> findByCategoryName(String categoryName);
    
    /**
     * 카테고리명으로 중복 체크 (대소문자 구분 안함)
     * 
     * @param categoryName 카테고리명
     * @return 존재 여부
     */
    boolean existsByCategoryNameIgnoreCase(String categoryName);
    
    // === 계층형 구조 관련 메서드 ===
    
    /**
     * 루트 카테고리 조회 (부모가 없는 카테고리)
     * 
     * @return 루트 카테고리 목록
     */
    @Query("SELECT c FROM Category c WHERE c.parentCategoryId IS NULL AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findRootCategories();
    
    /**
     * 부모 카테고리의 하위 카테고리 조회
     * 
     * @param parentCategoryId 부모 카테고리 ID
     * @return 하위 카테고리 목록
     */
    List<Category> findByParentCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Long parentCategoryId);
    
    /**
     * 특정 카테고리의 모든 하위 카테고리 조회 (재귀적)
     * 
     * @param parentCategoryId 부모 카테고리 ID
     * @return 모든 하위 카테고리 목록
     */
    @Query("SELECT c FROM Category c WHERE c.categoryPath LIKE CONCAT('%/', :parentCategoryId, '/%') AND c.isActive = true")
    List<Category> findAllSubCategories(@Param("parentCategoryId") Long parentCategoryId);
    
    /**
     * 카테고리 경로로 조회
     * 
     * @param categoryPath 카테고리 경로
     * @return 카테고리 Optional
     */
    Optional<Category> findByCategoryPath(String categoryPath);
    
    /**
     * 카테고리 경로가 특정 패턴으로 시작하는 카테고리들 조회
     * 
     * @param pathPrefix 경로 접두어
     * @return 카테고리 목록
     */
    List<Category> findByCategoryPathStartingWithAndIsActiveTrueOrderByDisplayOrderAsc(String pathPrefix);
    
    // === 카테고리 트리 구성을 위한 메서드 ===
    
    /**
     * 카테고리 트리 구성용 전체 활성 카테고리 조회
     * 부모 카테고리 ID와 표시 순서로 정렬
     * 
     * @return 전체 활성 카테고리 목록
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY COALESCE(c.parentCategoryId, 0), c.displayOrder")
    List<Category> findAllActiveForTree();
    
    /**
     * 특정 깊이의 카테고리들만 조회
     * 
     * @param depth 카테고리 깊이 (1: 루트, 2: 1차 하위, 3: 2차 하위...)
     * @return 해당 깊이의 카테고리 목록
     */
    @Query("SELECT c FROM Category c WHERE LENGTH(c.categoryPath) - LENGTH(REPLACE(c.categoryPath, '/', '')) - 1 = :depth AND c.isActive = true ORDER BY c.displayOrder")
    List<Category> findByDepth(@Param("depth") int depth);
    
    // === 관리 기능을 위한 메서드 ===
    
    /**
     * 특정 카테고리의 직계 하위 카테고리 개수 조회
     * 
     * @param parentCategoryId 부모 카테고리 ID
     * @return 하위 카테고리 개수
     */
    long countByParentCategoryId(Long parentCategoryId);
    
    /**
     * 카테고리에 속한 상품이 있는지 확인
     * 
     * @param categoryId 카테고리 ID
     * @return 상품 존재 여부
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.category.categoryId = :categoryId")
    boolean hasProducts(@Param("categoryId") Long categoryId);
    
    /**
     * 하위 카테고리가 있는지 확인
     * 
     * @param categoryId 카테고리 ID
     * @return 하위 카테고리 존재 여부
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.parentCategoryId = :categoryId")
    boolean hasSubCategories(@Param("categoryId") Long categoryId);
    
    /**
     * 같은 부모 카테고리 내에서 최대 표시 순서 조회
     * 
     * @param parentCategoryId 부모 카테고리 ID
     * @return 최대 표시 순서
     */
    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM Category c WHERE " +
           "(:parentCategoryId IS NULL AND c.parentCategoryId IS NULL) OR " +
           "(:parentCategoryId IS NOT NULL AND c.parentCategoryId = :parentCategoryId)")
    Integer findMaxDisplayOrderByParent(@Param("parentCategoryId") Long parentCategoryId);
    
    // === 검색 및 필터링 ===
    
    /**
     * 카테고리명으로 검색 (Like 조건, 활성화된 것만)
     * 
     * @param keyword 검색 키워드
     * @return 검색된 카테고리 목록
     */
    List<Category> findByCategoryNameContainingIgnoreCaseAndIsActiveTrueOrderByDisplayOrderAsc(String keyword);
    
    /**
     * 경로에 특정 키워드가 포함된 카테고리 검색
     * 
     * @param keyword 검색 키워드
     * @return 검색된 카테고리 목록
     */
    @Query("SELECT c FROM Category c WHERE c.categoryPath LIKE %:keyword% AND c.isActive = true ORDER BY c.displayOrder")
    List<Category> findByCategoryPathContaining(@Param("keyword") String keyword);
}