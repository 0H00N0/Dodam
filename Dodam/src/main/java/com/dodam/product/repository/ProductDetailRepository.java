package com.dodam.product.repository;

import com.dodam.product.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 상품 상세 Repository 인터페이스
 * 
 * <p>상품 상세 정보 엔티티의 데이터베이스 액세스를 담당합니다.</p>
 * 
 * @since 1.0.0
 */
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {
    
    // === 기본 조회 메서드 ===
    
    /**
     * 상품 ID로 상세 정보 조회
     * 
     * @param productId 상품 ID
     * @return 상품 상세 정보 Optional
     */
    Optional<ProductDetail> findByProductProductId(Long productId);
    
    /**
     * 상품 ID로 상세 정보 존재 여부 확인
     * 
     * @param productId 상품 ID
     * @return 존재 여부
     */
    boolean existsByProductProductId(Long productId);
    
    // === 검색 메서드 ===
    
    /**
     * 설명에 키워드가 포함된 상세 정보 조회
     * 
     * @param keyword 검색 키워드
     * @return 해당하는 상세 정보 목록
     */
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.description LIKE %:keyword%")
    List<ProductDetail> findByDescriptionContaining(@Param("keyword") String keyword);
    
    /**
     * 사양에 키워드가 포함된 상세 정보 조회
     * 
     * @param keyword 검색 키워드
     * @return 해당하는 상세 정보 목록
     */
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.specifications LIKE %:keyword%")
    List<ProductDetail> findBySpecificationsContaining(@Param("keyword") String keyword);
    
    /**
     * 특징에 키워드가 포함된 상세 정보 조회
     * 
     * @param keyword 검색 키워드
     * @return 해당하는 상세 정보 목록
     */
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.features LIKE %:keyword%")
    List<ProductDetail> findByFeaturesContaining(@Param("keyword") String keyword);
    
    /**
     * 소재별 상세 정보 조회
     * 
     * @param material 소재
     * @return 해당 소재의 상세 정보 목록
     */
    List<ProductDetail> findByMaterialContainingIgnoreCase(String material);
    
    /**
     * 원산지별 상세 정보 조회
     * 
     * @param originCountry 원산지
     * @return 해당 원산지의 상세 정보 목록
     */
    List<ProductDetail> findByOriginCountry(String originCountry);
    
    // === 조건별 조회 ===
    
    /**
     * 관리 방법이 있는 상세 정보 조회
     * 
     * @return 관리 방법이 있는 상세 정보 목록
     */
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.careInstructions IS NOT NULL AND pd.careInstructions != ''")
    List<ProductDetail> findWithCareInstructions();
    
    /**
     * 보증 정보가 있는 상세 정보 조회
     * 
     * @return 보증 정보가 있는 상세 정보 목록
     */
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.warrantyInfo IS NOT NULL AND pd.warrantyInfo != ''")
    List<ProductDetail> findWithWarrantyInfo();
    
    /**
     * 치수 정보가 있는 상세 정보 조회
     * 
     * @return 치수 정보가 있는 상세 정보 목록
     */
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.dimensions IS NOT NULL AND pd.dimensions != ''")
    List<ProductDetail> findWithDimensions();
    
    /**
     * 무게 정보가 있는 상세 정보 조회
     * 
     * @return 무게 정보가 있는 상세 정보 목록
     */
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.weight IS NOT NULL AND pd.weight != ''")
    List<ProductDetail> findWithWeight();
    
    // === 통계 및 집계 ===
    
    /**
     * 원산지별 상품 개수 조회
     * 
     * @return 원산지별 상품 개수 (Object[] 형태: [originCountry, count])
     */
    @Query("SELECT pd.originCountry, COUNT(pd) FROM ProductDetail pd " +
           "WHERE pd.originCountry IS NOT NULL " +
           "GROUP BY pd.originCountry " +
           "ORDER BY COUNT(pd) DESC")
    List<Object[]> countByOriginCountry();
    
    /**
     * 소재별 상품 개수 조회 (대소문자 무시)
     * 
     * @return 소재별 상품 개수 (Object[] 형태: [material, count])
     */
    @Query("SELECT UPPER(pd.material), COUNT(pd) FROM ProductDetail pd " +
           "WHERE pd.material IS NOT NULL " +
           "GROUP BY UPPER(pd.material) " +
           "ORDER BY COUNT(pd) DESC")
    List<Object[]> countByMaterial();
    
    /**
     * 전체 상세 정보 중 필수 정보 완성도 조회
     * (description, specifications, features가 모두 있는 것)
     * 
     * @return 완전한 상세 정보 개수
     */
    @Query("SELECT COUNT(pd) FROM ProductDetail pd " +
           "WHERE pd.description IS NOT NULL AND pd.description != '' " +
           "AND pd.specifications IS NOT NULL AND pd.specifications != '' " +
           "AND pd.features IS NOT NULL AND pd.features != ''")
    long countCompleteDetails();
    
    /**
     * 상세 정보가 불완전한 항목 개수
     * (description, specifications, features 중 하나라도 누락)
     * 
     * @return 불완전한 상세 정보 개수
     */
    @Query("SELECT COUNT(pd) FROM ProductDetail pd " +
           "WHERE pd.description IS NULL OR pd.description = '' " +
           "OR pd.specifications IS NULL OR pd.specifications = '' " +
           "OR pd.features IS NULL OR pd.features = ''")
    long countIncompleteDetails();
    
    // === JOIN 조회 (Product와 함께) ===
    
    /**
     * 특정 카테고리의 상세 정보 조회
     * 
     * @param categoryId 카테고리 ID
     * @return 해당 카테고리의 상세 정보 목록
     */
    @Query("SELECT pd FROM ProductDetail pd JOIN pd.product p WHERE p.category.categoryId = :categoryId")
    List<ProductDetail> findByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * 특정 브랜드의 상세 정보 조회
     * 
     * @param brandId 브랜드 ID
     * @return 해당 브랜드의 상세 정보 목록
     */
    @Query("SELECT pd FROM ProductDetail pd JOIN pd.product p WHERE p.brand.brandId = :brandId")
    List<ProductDetail> findByBrandId(@Param("brandId") Long brandId);
    
    /**
     * 활성 상품의 상세 정보만 조회
     * 
     * @return 활성 상품의 상세 정보 목록
     */
    @Query("SELECT pd FROM ProductDetail pd JOIN pd.product p WHERE p.status = 'ACTIVE'")
    List<ProductDetail> findActiveProductDetails();
    
    /**
     * 가격 범위에 해당하는 상품의 상세 정보 조회
     * 
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @return 해당 가격 범위의 상세 정보 목록
     */
    @Query("SELECT pd FROM ProductDetail pd JOIN pd.product p " +
           "WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<ProductDetail> findByPriceRange(@Param("minPrice") Long minPrice, 
                                        @Param("maxPrice") Long maxPrice);
}