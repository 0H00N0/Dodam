package com.dodam.product.repository;

import com.dodam.product.entity.Product;
import com.dodam.product.common.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 상품 Repository 인터페이스
 * 
 * <p>상품 엔티티의 데이터베이스 액세스를 담당합니다.</p>
 * 
 * @since 1.0.0
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // === 기본 조회 메서드 ===
    
    /**
     * 상품 상태별 조회 (페이징)
     * 
     * @param status 상품 상태
     * @param pageable 페이징 정보
     * @return 상품 페이지
     */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    
    /**
     * 카테고리별 상품 조회
     * 
     * @param categoryId 카테고리 ID
     * @param pageable 페이징 정보
     * @return 상품 페이지
     */
    Page<Product> findByCategoryCategoryId(Long categoryId, Pageable pageable);
    
    /**
     * 브랜드별 상품 조회
     * 
     * @param brandId 브랜드 ID
     * @param pageable 페이징 정보
     * @return 상품 페이지
     */
    Page<Product> findByBrandBrandId(Long brandId, Pageable pageable);
    
    /**
     * 상품명으로 검색 (Like 조건, 대소문자 구분 안함)
     * 
     * @param productName 검색할 상품명
     * @param pageable 페이징 정보
     * @return 상품 페이지
     */
    Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);
    
    // === N+1 문제 해결을 위한 EntityGraph 활용 ===
    
    /**
     * 상세 정보와 함께 상품 조회 (N+1 문제 해결)
     * 
     * @param productId 상품 ID
     * @return 상품 Optional
     */
    @EntityGraph(attributePaths = {"category", "brand", "detail", "inventory"})
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findByIdWithDetails(@Param("productId") Long productId);
    
    /**
     * 옵션과 이미지를 포함한 상품 상세 조회
     * 
     * @param productId 상품 ID
     * @return 상품 Optional
     */
    @EntityGraph(attributePaths = {"category", "brand", "options", "images", "detail", "inventory"})
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findByIdWithFullDetails(@Param("productId") Long productId);
    
    /**
     * 상태별 상품 목록 조회 (연관 엔티티 포함)
     * 
     * @param status 상품 상태
     * @param pageable 페이징 정보
     * @return 상품 페이지
     */
    @EntityGraph(attributePaths = {"category", "brand", "inventory"})
    @Query("SELECT p FROM Product p WHERE p.status = :status")
    Page<Product> findByStatusWithDetails(@Param("status") ProductStatus status, Pageable pageable);
    
    // === 복잡한 조회 쿼리 ===
    
    /**
     * 가격 범위와 카테고리로 상품 검색
     * 
     * @param categoryId 카테고리 ID
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param status 상품 상태
     * @param pageable 페이징 정보
     * @return 상품 페이지
     */
    @Query("SELECT p FROM Product p " +
           "WHERE (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND p.status = :status")
    Page<Product> findBySearchCriteria(
        @Param("categoryId") Long categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("status") ProductStatus status,
        Pageable pageable
    );
    
    /**
     * 키워드로 상품명, 브랜드명, 카테고리명에서 검색
     * 
     * @param keyword 검색 키워드
     * @param status 상품 상태
     * @param pageable 페이징 정보
     * @return 상품 페이지
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN p.brand b " +
           "LEFT JOIN p.category c " +
           "WHERE p.status = :status " +
           "AND (LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.brandName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> findByKeywordSearch(@Param("keyword") String keyword, 
                                     @Param("status") ProductStatus status, 
                                     Pageable pageable);
    
    /**
     * 인기 상품 조회 (주문량 기준, 추후 주문 도메인과 연결)
     * 
     * @param limit 조회할 개수
     * @return 인기 상품 리스트
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.status = 'ACTIVE' " +
           "ORDER BY p.productId DESC")  // 임시로 최신 등록 순으로 정렬
    List<Product> findPopularProducts(@Param("limit") int limit);
    
    // === 통계 및 집계 쿼리 ===
    
    /**
     * 카테고리별 상품 개수 조회
     * 
     * @param categoryId 카테고리 ID
     * @param status 상품 상태
     * @return 상품 개수
     */
    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.category.categoryId = :categoryId " +
           "AND p.status = :status")
    long countByCategoryAndStatus(@Param("categoryId") Long categoryId, 
                                 @Param("status") ProductStatus status);
    
    /**
     * 브랜드별 상품 개수 조회
     * 
     * @param brandId 브랜드 ID
     * @param status 상품 상태
     * @return 상품 개수
     */
    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.brand.brandId = :brandId " +
           "AND p.status = :status")
    long countByBrandAndStatus(@Param("brandId") Long brandId, 
                              @Param("status") ProductStatus status);
    
    /**
     * 가격 범위별 상품 개수 조회
     * 
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param status 상품 상태
     * @return 상품 개수
     */
    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.price BETWEEN :minPrice AND :maxPrice " +
           "AND p.status = :status")
    long countByPriceRangeAndStatus(@Param("minPrice") BigDecimal minPrice, 
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   @Param("status") ProductStatus status);
    
    // === 업데이트 쿼리 ===
    
    /**
     * 상품 상태 일괄 업데이트
     * 
     * @param productIds 상품 ID 목록
     * @param status 변경할 상태
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Product p SET p.status = :status, p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.productId IN :productIds")
    int updateStatusByIds(@Param("productIds") List<Long> productIds, 
                         @Param("status") ProductStatus status);
    
    /**
     * 특정 브랜드의 모든 상품 상태 업데이트
     * 
     * @param brandId 브랜드 ID
     * @param status 변경할 상태
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Product p SET p.status = :status, p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.brand.brandId = :brandId")
    int updateStatusByBrandId(@Param("brandId") Long brandId, 
                             @Param("status") ProductStatus status);
}