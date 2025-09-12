package com.dodam.product.repository;

import com.dodam.product.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 카테고리 Repository 인터페이스
 * 카테고리 정보에 대한 데이터 접근 및 쿼리를 담당합니다.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ========================== 기본 검색 메소드 ==========================
    
    /**
     * 카테고리명으로 카테고리 검색
     * @param categoryName 카테고리명
     * @return 카테고리 정보 (Optional)
     */
    Optional<Category> findByCategoryName(String categoryName);

    /**
     * 카테고리명으로 카테고리 존재 여부 확인
     * @param categoryName 카테고리명
     * @return 존재 여부
     */
    boolean existsByCategoryName(String categoryName);

    /**
     * 삭제되지 않은 모든 카테고리 조회 (정렬 지원)
     * @param sort 정렬 조건
     * @return 활성 카테고리 목록
     */
    List<Category> findByDeletedAtIsNull(Sort sort);

    /**
     * 삭제되지 않은 카테고리 페이징 조회
     * @param pageable 페이징 정보
     * @return 활성 카테고리 페이지
     */
    Page<Category> findByDeletedAtIsNull(Pageable pageable);

    /**
     * 삭제된 카테고리 조회
     * @param pageable 페이징 정보
     * @return 삭제된 카테고리 페이지
     */
    Page<Category> findByDeletedAtIsNotNull(Pageable pageable);

    // ========================== 검색 메소드 ==========================

    /**
     * 카테고리명에 키워드가 포함된 활성 카테고리 검색
     * @param keyword 검색 키워드
     * @return 매칭된 카테고리 목록
     */
    List<Category> findByCategoryNameContainingAndDeletedAtIsNull(String keyword);

    /**
     * 카테고리명에 키워드가 포함된 활성 카테고리 페이징 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 매칭된 카테고리 페이지
     */
    Page<Category> findByCategoryNameContainingAndDeletedAtIsNull(String keyword, Pageable pageable);

    /**
     * 설명에 키워드가 포함된 활성 카테고리 검색
     * @param keyword 검색 키워드
     * @return 매칭된 카테고리 목록
     */
    List<Category> findByDescriptionContainingAndDeletedAtIsNull(String keyword);

    /**
     * 생성일 기간으로 카테고리 검색
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 기간 내 생성된 카테고리 목록
     */
    List<Category> findByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime startDate, LocalDateTime endDate);

    // ========================== 복합 검색 메소드 ==========================

    /**
     * 카테고리명 또는 설명에 키워드가 포함된 활성 카테고리 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 매칭된 카테고리 페이지
     */
    @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL AND " +
           "(c.categoryName LIKE CONCAT('%', :keyword, '%') OR c.description LIKE CONCAT('%', :keyword, '%'))")
    Page<Category> searchActiveCategories(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 상품을 가진 활성 카테고리만 조회
     * @return 상품이 있는 카테고리 목록
     */
    @Query("SELECT DISTINCT c FROM Category c " +
           "JOIN c.products p " +
           "WHERE c.deletedAt IS NULL AND p.deletedAt IS NULL")
    List<Category> findCategoriesWithProducts();

    /**
     * 상품을 가진 활성 카테고리 페이징 조회
     * @param pageable 페이징 정보
     * @return 상품이 있는 카테고리 페이지
     */
    @Query("SELECT DISTINCT c FROM Category c " +
           "JOIN c.products p " +
           "WHERE c.deletedAt IS NULL AND p.deletedAt IS NULL")
    Page<Category> findCategoriesWithProducts(Pageable pageable);

    // ========================== 통계 및 집계 쿼리 ==========================

    /**
     * 전체 활성 카테고리 수 조회
     * @return 활성 카테고리 수
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.deletedAt IS NULL")
    long countActiveCategories();

    /**
     * 전체 삭제된 카테고리 수 조회
     * @return 삭제된 카테고리 수
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.deletedAt IS NOT NULL")
    long countDeletedCategories();

    /**
     * 상품이 있는 카테고리 수 조회
     * @return 상품이 있는 카테고리 수
     */
    @Query("SELECT COUNT(DISTINCT c) FROM Category c " +
           "JOIN c.products p " +
           "WHERE c.deletedAt IS NULL AND p.deletedAt IS NULL")
    long countCategoriesWithProducts();

    /**
     * 상품이 없는 빈 카테고리 수 조회
     * @return 빈 카테고리 수
     */
    @Query("SELECT COUNT(c) FROM Category c " +
           "WHERE c.deletedAt IS NULL AND " +
           "NOT EXISTS (SELECT 1 FROM Product p WHERE p.category.categoryId = c.categoryId AND p.deletedAt IS NULL)")
    long countEmptyCategories();

    /**
     * 각 카테고리별 상품 수 통계 조회
     * @return 카테고리별 상품 수 [카테고리명, 상품수]
     */
    @Query("SELECT c.categoryName, COUNT(p) FROM Category c " +
           "LEFT JOIN c.products p ON p.deletedAt IS NULL " +
           "WHERE c.deletedAt IS NULL " +
           "GROUP BY c.categoryId, c.categoryName " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> getCategoryProductCountStats();

    /**
     * 최근 생성된 카테고리 조회 (상위 N개)
     * @param pageable 페이징 정보 (size로 개수 제한)
     * @return 최근 생성된 카테고리 목록
     */
    @Query("SELECT c FROM Category c " +
           "WHERE c.deletedAt IS NULL " +
           "ORDER BY c.createdAt DESC")
    Slice<Category> findRecentCategories(Pageable pageable);

    // ========================== DTO Projection 쿼리 ==========================

    /**
     * 카테고리 기본 정보만 조회 (성능 최적화)
     * @return [카테고리ID, 카테고리명, 생성일시]
     */
    @Query("SELECT c.categoryId, c.categoryName, c.createdAt FROM Category c " +
           "WHERE c.deletedAt IS NULL " +
           "ORDER BY c.categoryName")
    List<Object[]> findCategoryBasicInfo();

    /**
     * 카테고리별 상품 통계 정보 조회
     * @return [카테고리ID, 카테고리명, 상품수, 평균가격, 최고가격, 최저가격]
     */
    @Query("SELECT c.categoryId, c.categoryName, COUNT(p), " +
           "COALESCE(AVG(p.price), 0), COALESCE(MAX(p.price), 0), COALESCE(MIN(p.price), 0) " +
           "FROM Category c " +
           "LEFT JOIN c.products p ON p.deletedAt IS NULL AND p.status = com.dodam.product.entity.Product$ProductStatus.ACTIVE " +
           "WHERE c.deletedAt IS NULL " +
           "GROUP BY c.categoryId, c.categoryName " +
           "ORDER BY c.categoryName")
    List<Object[]> getCategoryProductStats();

    // ========================== 수정 및 삭제 메소드 ==========================

    /**
     * 카테고리 소프트 삭제 (Bulk Update)
     * @param categoryId 카테고리 ID
     * @param deletedAt 삭제 시간
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Category c SET c.deletedAt = :deletedAt WHERE c.categoryId = :categoryId")
    int softDeleteCategory(@Param("categoryId") Long categoryId, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 카테고리 복구 (Bulk Update)
     * @param categoryId 카테고리 ID
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Category c SET c.deletedAt = NULL WHERE c.categoryId = :categoryId")
    int restoreCategory(@Param("categoryId") Long categoryId);

    /**
     * 빈 카테고리들 일괄 삭제 (상품이 없는 카테고리)
     * @param deletedAt 삭제 시간
     * @return 삭제된 카테고리 수
     */
    @Modifying
    @Query("UPDATE Category c SET c.deletedAt = :deletedAt " +
           "WHERE c.deletedAt IS NULL AND " +
           "NOT EXISTS (SELECT 1 FROM Product p WHERE p.category.categoryId = c.categoryId AND p.deletedAt IS NULL)")
    int softDeleteEmptyCategories(@Param("deletedAt") LocalDateTime deletedAt);

    // ========================== 특수 쿼리 ==========================

    /**
     * 카테고리명 중복 검사 (대소문자 무시)
     * @param categoryName 검사할 카테고리명
     * @param excludeId 제외할 카테고리 ID (수정 시 사용)
     * @return 중복 여부
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c " +
           "WHERE LOWER(c.categoryName) = LOWER(:categoryName) " +
           "AND c.deletedAt IS NULL " +
           "AND (:excludeId IS NULL OR c.categoryId != :excludeId)")
    boolean existsByCategoryNameIgnoreCase(@Param("categoryName") String categoryName, 
                                          @Param("excludeId") Long excludeId);

    /**
     * 활성 카테고리의 총 상품 수 조회
     * @return 모든 활성 카테고리의 상품 합계
     */
    @Query("SELECT COUNT(p) FROM Category c " +
           "JOIN c.products p " +
           "WHERE c.deletedAt IS NULL AND p.deletedAt IS NULL")
    long getTotalProductCountInActiveCategories();

    /**
     * 특정 기간 내 생성된 카테고리 수 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 기간 내 생성된 카테고리 수
     */
    @Query("SELECT COUNT(c) FROM Category c " +
           "WHERE c.createdAt BETWEEN :startDate AND :endDate")
    long countCategoriesCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * 카테고리와 함께 상품 정보를 fetch join으로 조회 (N+1 문제 해결)
     * @param categoryId 카테고리 ID
     * @return 상품 정보가 포함된 카테고리
     */
    @Query("SELECT c FROM Category c " +
           "LEFT JOIN FETCH c.products p " +
           "WHERE c.categoryId = :categoryId AND c.deletedAt IS NULL")
    Optional<Category> findByIdWithProducts(@Param("categoryId") Long categoryId);

    /**
     * 인기 카테고리 조회 (상품 수 기준 상위 N개)
     * @param pageable 페이징 정보
     * @return 인기 카테고리 목록
     */
    @Query("SELECT c FROM Category c " +
           "LEFT JOIN c.products p ON p.deletedAt IS NULL " +
           "WHERE c.deletedAt IS NULL " +
           "GROUP BY c.categoryId " +
           "ORDER BY COUNT(p) DESC")
    Slice<Category> findPopularCategories(Pageable pageable);

    /**
     * 특정 카테고리의 상품 수 조회
     * @param categoryId 카테고리 ID
     * @return 해당 카테고리의 상품 수
     */
    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.category.categoryId = :categoryId AND p.deletedAt IS NULL")
    long countProductsByCategoryId(@Param("categoryId") Long categoryId);
}