package com.dodam.product.repository;

import com.dodam.product.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * 브랜드 Repository 인터페이스
 * 
 * <p>브랜드 엔티티의 데이터베이스 액세스를 담당합니다.</p>
 * 
 * @since 1.0.0
 */
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    // === 기본 조회 메서드 ===
    
    /**
     * 활성화된 브랜드만 조회 (이름 순 정렬)
     * 
     * @return 활성 브랜드 목록
     */
    List<Brand> findByIsActiveTrueOrderByBrandNameAsc();
    
    /**
     * 브랜드명으로 조회
     * 
     * @param brandName 브랜드명
     * @return 브랜드 Optional
     */
    Optional<Brand> findByBrandName(String brandName);
    
    /**
     * 브랜드명으로 조회 (대소문자 구분 안함)
     * 
     * @param brandName 브랜드명
     * @return 브랜드 Optional
     */
    Optional<Brand> findByBrandNameIgnoreCase(String brandName);
    
    /**
     * 브랜드명 중복 체크
     * 
     * @param brandName 브랜드명
     * @return 존재 여부
     */
    boolean existsByBrandName(String brandName);
    
    /**
     * 브랜드명 중복 체크 (대소문자 구분 안함)
     * 
     * @param brandName 브랜드명
     * @return 존재 여부
     */
    boolean existsByBrandNameIgnoreCase(String brandName);
    
    /**
     * 특정 브랜드를 제외하고 브랜드명 중복 체크
     * 
     * @param brandName 브랜드명
     * @param excludeId 제외할 브랜드 ID
     * @return 존재 여부
     */
    @Query("SELECT COUNT(b) > 0 FROM Brand b WHERE LOWER(b.brandName) = LOWER(:brandName) AND b.brandId != :excludeId")
    boolean existsByBrandNameIgnoreCaseExcludingId(@Param("brandName") String brandName, 
                                                  @Param("excludeId") Long excludeId);
    
    // === 검색 및 필터링 ===
    
    /**
     * 브랜드명으로 검색 (Like 조건, 활성화된 것만)
     * 
     * @param keyword 검색 키워드
     * @return 검색된 브랜드 목록
     */
    List<Brand> findByBrandNameContainingIgnoreCaseAndIsActiveTrueOrderByBrandNameAsc(String keyword);
    
    /**
     * 브랜드명 또는 설명으로 검색
     * 
     * @param keyword 검색 키워드
     * @return 검색된 브랜드 목록
     */
    @Query("SELECT b FROM Brand b WHERE b.isActive = true AND " +
           "(LOWER(b.brandName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY b.brandName ASC")
    List<Brand> findByKeywordSearch(@Param("keyword") String keyword);
    
    // === 통계 및 관리 정보 ===
    
    /**
     * 브랜드에 속한 상품 개수 조회
     * 
     * @param brandId 브랜드 ID
     * @return 상품 개수
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.brandId = :brandId")
    long countProducts(@Param("brandId") Long brandId);
    
    /**
     * 브랜드에 속한 활성 상품 개수 조회
     * 
     * @param brandId 브랜드 ID
     * @return 활성 상품 개수
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.brandId = :brandId AND p.status = 'ACTIVE'")
    long countActiveProducts(@Param("brandId") Long brandId);
    
    /**
     * 상품이 있는 브랜드인지 확인
     * 
     * @param brandId 브랜드 ID
     * @return 상품 존재 여부
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.brand.brandId = :brandId")
    boolean hasProducts(@Param("brandId") Long brandId);
    
    /**
     * 인기 브랜드 조회 (상품 개수 기준)
     * 
     * @param limit 조회할 개수
     * @return 인기 브랜드 목록
     */
    @Query("SELECT b FROM Brand b " +
           "WHERE b.isActive = true " +
           "ORDER BY (SELECT COUNT(p) FROM Product p WHERE p.brand.brandId = b.brandId) DESC")
    List<Brand> findPopularBrands(@Param("limit") int limit);
    
    /**
     * 로고가 있는 브랜드만 조회
     * 
     * @return 로고가 있는 브랜드 목록
     */
    @Query("SELECT b FROM Brand b WHERE b.isActive = true AND b.brandLogoUrl IS NOT NULL AND b.brandLogoUrl != '' ORDER BY b.brandName ASC")
    List<Brand> findWithLogo();
    
    /**
     * 최근 추가된 브랜드 조회
     * 
     * @param limit 조회할 개수
     * @return 최신 브랜드 목록
     */
    @Query("SELECT b FROM Brand b WHERE b.isActive = true ORDER BY b.brandId DESC")
    List<Brand> findRecentBrands(@Param("limit") int limit);
    
    // === 관리용 쿼리 ===
    
    /**
     * 활성화된 브랜드 개수 조회
     * 
     * @return 활성 브랜드 개수
     */
    long countByIsActiveTrue();
    
    /**
     * 비활성화된 브랜드 개수 조회
     * 
     * @return 비활성 브랜드 개수
     */
    long countByIsActiveFalse();
    
    /**
     * 브랜드명 첫 글자별 그룹핑 (한글, 영문 분류용)
     * 
     * @return 브랜드명 첫 글자별 통계
     */
    @Query("SELECT SUBSTRING(b.brandName, 1, 1) as firstChar, COUNT(b) as brandCount " +
           "FROM Brand b WHERE b.isActive = true " +
           "GROUP BY SUBSTRING(b.brandName, 1, 1) " +
           "ORDER BY firstChar")
    List<Object[]> findBrandCountByFirstCharacter();
}