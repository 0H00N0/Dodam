package com.dodam.product.repository;

import com.dodam.product.entity.Product;
import com.dodam.product.entity.Product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 상품 Repository 인터페이스
 * 상품 정보에 대한 데이터 접근 및 쿼리를 담당합니다.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ========================== 기본 검색 메소드 ==========================

    /**
     * 상품명으로 상품 검색
     * @param productName 상품명
     * @return 상품 정보 (Optional)
     */
    Optional<Product> findByProductName(String productName);

    /**
     * 상품명으로 상품 존재 여부 확인
     * @param productName 상품명
     * @return 존재 여부
     */
    boolean existsByProductName(String productName);

    /**
     * 삭제되지 않은 모든 상품 조회 (정렬 지원)
     * @param sort 정렬 조건
     * @return 활성 상품 목록
     */
    List<Product> findByDeletedAtIsNull(Sort sort);

    /**
     * 삭제되지 않은 상품 페이징 조회
     * @param pageable 페이징 정보
     * @return 활성 상품 페이지
     */
    Page<Product> findByDeletedAtIsNull(Pageable pageable);

    /**
     * 상품 상태별 조회
     * @param status 상품 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 상품 페이지
     */
    Page<Product> findByStatusAndDeletedAtIsNull(ProductStatus status, Pageable pageable);

    // ========================== 카테고리별 검색 메소드 ==========================

    /**
     * 카테고리별 활성 상품 조회
     * @param categoryId 카테고리 ID
     * @param pageable 페이징 정보
     * @return 해당 카테고리의 상품 페이지
     */
    Page<Product> findByCategoryCategoryIdAndDeletedAtIsNull(Long categoryId, Pageable pageable);

    /**
     * 카테고리별 특정 상태 상품 조회
     * @param categoryId 카테고리 ID
     * @param status 상품 상태
     * @param pageable 페이징 정보
     * @return 해당 조건의 상품 페이지
     */
    Page<Product> findByCategoryCategoryIdAndStatusAndDeletedAtIsNull(Long categoryId, ProductStatus status, Pageable pageable);

    /**
     * 카테고리별 상품 수 조회
     * @param categoryId 카테고리 ID
     * @return 해당 카테고리의 상품 수
     */
    long countByCategoryCategoryIdAndDeletedAtIsNull(Long categoryId);

    // ========================== 가격별 검색 메소드 ==========================

    /**
     * 가격 범위로 상품 검색
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param pageable 페이징 정보
     * @return 가격 범위 내 상품 페이지
     */
    Page<Product> findByPriceBetweenAndDeletedAtIsNull(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * 특정 가격 이하의 상품 검색
     * @param maxPrice 최대 가격
     * @param pageable 페이징 정보
     * @return 가격 이하 상품 페이지
     */
    Page<Product> findByPriceLessThanEqualAndDeletedAtIsNull(BigDecimal maxPrice, Pageable pageable);

    /**
     * 특정 가격 이상의 상품 검색
     * @param minPrice 최소 가격
     * @param pageable 페이징 정보
     * @return 가격 이상 상품 페이지
     */
    Page<Product> findByPriceGreaterThanEqualAndDeletedAtIsNull(BigDecimal minPrice, Pageable pageable);

    // ========================== 재고별 검색 메소드 ==========================

    /**
     * 재고 부족 상품 조회 (지정 수량 이하)
     * @param threshold 임계값
     * @param pageable 페이징 정보
     * @return 재고 부족 상품 페이지
     */
    Page<Product> findByStockQuantityLessThanAndDeletedAtIsNull(Integer threshold, Pageable pageable);

    /**
     * 품절 상품 조회
     * @param pageable 페이징 정보
     * @return 품절 상품 페이지
     */
    Page<Product> findByStockQuantityAndDeletedAtIsNull(Integer stockQuantity, Pageable pageable);

    /**
     * 재고 있는 상품 조회
     * @param pageable 페이징 정보
     * @return 재고 있는 상품 페이지
     */
    Page<Product> findByStockQuantityGreaterThanAndDeletedAtIsNull(Integer stockQuantity, Pageable pageable);

    // ========================== 복합 검색 메소드 ==========================

    /**
     * 상품명에 키워드가 포함된 활성 상품 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 매칭된 상품 페이지
     */
    Page<Product> findByProductNameContainingAndDeletedAtIsNull(String keyword, Pageable pageable);

    /**
     * 카테고리와 가격 범위로 상품 검색
     * @param categoryId 카테고리 ID
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param pageable 페이징 정보
     * @return 조건에 맞는 상품 페이지
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.category.categoryId = :categoryId " +
           "AND p.price BETWEEN :minPrice AND :maxPrice " +
           "AND p.deletedAt IS NULL")
    Page<Product> findByCategoryAndPriceRange(@Param("categoryId") Long categoryId,
                                              @Param("minPrice") BigDecimal minPrice,
                                              @Param("maxPrice") BigDecimal maxPrice,
                                              Pageable pageable);

    /**
     * 종합 검색 (상품명, 설명에서 키워드 검색)
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 매칭된 상품 페이지
     */
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND " +
           "(p.productName LIKE CONCAT('%', :keyword, '%') OR p.description LIKE CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 고급 상품 검색 (카테고리, 가격, 재고 조건 복합)
     * @param categoryId 카테고리 ID (null 허용)
     * @param minPrice 최소 가격 (null 허용)
     * @param maxPrice 최대 가격 (null 허용)
     * @param status 상품 상태 (null 허용)
     * @param minStock 최소 재고 (null 허용)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 상품 페이지
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.deletedAt IS NULL " +
           "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:minStock IS NULL OR p.stockQuantity >= :minStock)")
    Page<Product> advancedSearch(@Param("categoryId") Long categoryId,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("status") ProductStatus status,
                                 @Param("minStock") Integer minStock,
                                 Pageable pageable);

    // ========================== 통계 및 집계 쿼리 ==========================

    /**
     * 전체 활성 상품 수 조회
     * @return 활성 상품 수
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.deletedAt IS NULL")
    long countActiveProducts();

    /**
     * 상태별 상품 수 조회
     * @param status 상품 상태
     * @return 해당 상태의 상품 수
     */
    long countByStatusAndDeletedAtIsNull(ProductStatus status);

    /**
     * 카테고리별 상품 통계 조회
     * @return [카테고리ID, 카테고리명, 상품수, 평균가격, 총재고]
     */
    @Query("SELECT c.categoryId, c.categoryName, COUNT(p), " +
           "COALESCE(AVG(p.price), 0), COALESCE(SUM(p.stockQuantity), 0) " +
           "FROM Category c " +
           "LEFT JOIN c.products p ON p.deletedAt IS NULL " +
           "WHERE c.deletedAt IS NULL " +
           "GROUP BY c.categoryId, c.categoryName " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> getCategoryProductStats();

    /**
     * 가격 통계 조회
     * @return [최고가, 최저가, 평균가격]
     */
    @Query("SELECT MAX(p.price), MIN(p.price), AVG(p.price) " +
           "FROM Product p WHERE p.deletedAt IS NULL")
    Object[] getPriceStats();

    /**
     * 재고 통계 조회
     * @return [총재고, 평균재고, 재고부족상품수]
     */
    @Query("SELECT SUM(p.stockQuantity), AVG(p.stockQuantity), " +
           "COUNT(CASE WHEN p.stockQuantity < :threshold THEN 1 END) " +
           "FROM Product p WHERE p.deletedAt IS NULL")
    Object[] getStockStats(@Param("threshold") Integer threshold);

    // ========================== 인기/추천 상품 쿼리 ==========================

    /**
     * 최신 상품 조회
     * @param pageable 페이징 정보
     * @return 최신 상품 목록
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.deletedAt IS NULL AND p.status = com.dodam.product.entity.Product$ProductStatus.ACTIVE " +
           "ORDER BY p.createdAt DESC")
    Slice<Product> findLatestProducts(Pageable pageable);

    /**
     * 리뷰가 많은 인기 상품 조회
     * @param pageable 페이징 정보
     * @return 인기 상품 목록
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN p.reviews r ON r.deletedAt IS NULL " +
           "WHERE p.deletedAt IS NULL AND p.status = com.dodam.product.entity.Product$ProductStatus.ACTIVE " +
           "GROUP BY p.productId " +
           "ORDER BY COUNT(r) DESC")
    Slice<Product> findPopularProductsByReviewCount(Pageable pageable);

    /**
     * 평점이 높은 상품 조회 (평점 4점 이상, 리뷰 5개 이상)
     * @param minRating 최소 평점
     * @param minReviewCount 최소 리뷰 수
     * @param pageable 페이징 정보
     * @return 고평점 상품 목록
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN p.reviews r ON r.deletedAt IS NULL " +
           "WHERE p.deletedAt IS NULL AND p.status = com.dodam.product.entity.Product$ProductStatus.ACTIVE " +
           "GROUP BY p.productId " +
           "HAVING COUNT(r) >= :minReviewCount AND AVG(r.rating) >= :minRating " +
           "ORDER BY AVG(r.rating) DESC, COUNT(r) DESC")
    Slice<Product> findHighRatedProducts(@Param("minRating") Double minRating,
                                        @Param("minReviewCount") Long minReviewCount,
                                        Pageable pageable);

    /**
     * 할인 상품 조회 (원래 가격 대비 할인된 상품)
     * @param maxPrice 할인 기준 최대 가격
     * @param pageable 페이징 정보
     * @return 할인 상품 목록
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.deletedAt IS NULL AND p.status = com.dodam.product.entity.Product$ProductStatus.ACTIVE " +
           "AND p.price <= :maxPrice " +
           "ORDER BY p.price ASC")
    Slice<Product> findDiscountProducts(@Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    // ========================== 리뷰 통계와 함께 조회 ==========================

    /**
     * 상품과 리뷰 통계 함께 조회
     * @param productId 상품 ID
     * @return [상품정보, 리뷰수, 평균평점]
     */
    @Query("SELECT p, COUNT(r), COALESCE(AVG(r.rating), 0) " +
           "FROM Product p " +
           "LEFT JOIN p.reviews r ON r.deletedAt IS NULL " +
           "WHERE p.productId = :productId AND p.deletedAt IS NULL " +
           "GROUP BY p.productId")
    Object[] findProductWithReviewStats(@Param("productId") Long productId);

    /**
     * 상품별 리뷰 통계 목록 조회
     * @param pageable 페이징 정보
     * @return [상품ID, 상품명, 리뷰수, 평균평점, 가격]
     */
    @Query("SELECT p.productId, p.productName, COUNT(r), " +
           "COALESCE(AVG(r.rating), 0), p.price " +
           "FROM Product p " +
           "LEFT JOIN p.reviews r ON r.deletedAt IS NULL " +
           "WHERE p.deletedAt IS NULL " +
           "GROUP BY p.productId, p.productName, p.price " +
           "ORDER BY COUNT(r) DESC")
    Page<Object[]> findProductsWithReviewStats(Pageable pageable);

    // ========================== Fetch Join 쿼리 (N+1 문제 해결) ==========================

    /**
     * 상품과 카테고리 정보를 fetch join으로 조회
     * @param productId 상품 ID
     * @return 카테고리 정보가 포함된 상품
     */
    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.category c " +
           "WHERE p.productId = :productId AND p.deletedAt IS NULL")
    Optional<Product> findByIdWithCategory(@Param("productId") Long productId);

    /**
     * 상품과 리뷰 목록을 fetch join으로 조회
     * @param productId 상품 ID
     * @return 리뷰 목록이 포함된 상품
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.reviews r " +
           "WHERE p.productId = :productId AND p.deletedAt IS NULL")
    Optional<Product> findByIdWithReviews(@Param("productId") Long productId);

    // ========================== 수정 및 삭제 메소드 ==========================

    /**
     * 상품 재고 업데이트 (Bulk Update)
     * @param productId 상품 ID
     * @param newStock 새로운 재고 수량
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = :newStock, p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.productId = :productId")
    int updateStock(@Param("productId") Long productId, @Param("newStock") Integer newStock);

    /**
     * 상품 가격 업데이트 (Bulk Update)
     * @param productId 상품 ID
     * @param newPrice 새로운 가격
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Product p SET p.price = :newPrice, p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.productId = :productId")
    int updatePrice(@Param("productId") Long productId, @Param("newPrice") BigDecimal newPrice);

    /**
     * 상품 상태 업데이트 (Bulk Update)
     * @param productId 상품 ID
     * @param status 새로운 상태
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Product p SET p.status = :status, p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.productId = :productId")
    int updateStatus(@Param("productId") Long productId, @Param("status") ProductStatus status);

    /**
     * 상품 소프트 삭제 (Bulk Update)
     * @param productId 상품 ID
     * @param deletedAt 삭제 시간
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Product p SET p.deletedAt = :deletedAt, p.status = com.dodam.product.entity.Product$ProductStatus.INACTIVE " +
           "WHERE p.productId = :productId")
    int softDeleteProduct(@Param("productId") Long productId, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 품절 상품 상태 일괄 업데이트
     * @return 업데이트된 상품 수
     */
    @Modifying
    @Query("UPDATE Product p SET p.status = com.dodam.product.entity.Product$ProductStatus.OUT_OF_STOCK, p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.stockQuantity = 0 AND p.status = com.dodam.product.entity.Product$ProductStatus.ACTIVE AND p.deletedAt IS NULL")
    int updateOutOfStockProducts();

    /**
     * 재고 부족 상품에 대한 상태 업데이트
     * @param threshold 임계값 (이하면 경고 상태)
     * @return 업데이트된 상품 수
     */
    @Modifying
    @Query("UPDATE Product p SET p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.stockQuantity <= :threshold AND p.stockQuantity > 0 " +
           "AND p.status = com.dodam.product.entity.Product$ProductStatus.ACTIVE AND p.deletedAt IS NULL")
    int markLowStockProducts(@Param("threshold") Integer threshold);

    // ========================== 특수 쿼리 ==========================

    /**
     * 특정 기간 내 등록된 상품 수 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 기간 내 등록된 상품 수
     */
    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.createdAt BETWEEN :startDate AND :endDate")
    long countProductsCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * 상품명 중복 검사 (대소문자 무시)
     * @param productName 검사할 상품명
     * @param excludeId 제외할 상품 ID (수정 시 사용)
     * @return 중복 여부
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p " +
           "WHERE LOWER(p.productName) = LOWER(:productName) " +
           "AND p.deletedAt IS NULL " +
           "AND (:excludeId IS NULL OR p.productId != :excludeId)")
    boolean existsByProductNameIgnoreCase(@Param("productName") String productName,
                                         @Param("excludeId") Long excludeId);

    /**
     * 카테고리 변경이 가능한 상품들 조회 (리뷰가 없는 상품)
     * @param pageable 페이징 정보
     * @return 카테고리 변경 가능한 상품 목록
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.deletedAt IS NULL " +
           "AND NOT EXISTS (SELECT 1 FROM Review r WHERE r.product.productId = p.productId AND r.deletedAt IS NULL)")
    Page<Product> findProductsWithoutReviews(Pageable pageable);
}