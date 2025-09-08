package com.dodam.product.repository;

import com.dodam.product.entity.ProductImage;
import com.dodam.product.common.enums.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 상품 이미지 Repository 인터페이스
 * 
 * <p>상품 이미지 엔티티의 데이터베이스 액세스를 담당합니다.</p>
 * 
 * @since 1.0.0
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    // === 기본 조회 메서드 ===
    
    /**
     * 상품 ID로 이미지 목록 조회 (순서대로 정렬)
     * 
     * @param productId 상품 ID
     * @return 이미지 목록 (순서대로 정렬됨)
     */
    List<ProductImage> findByProductProductIdOrderByImageOrderAsc(Long productId);
    
    /**
     * 상품 ID와 이미지 타입으로 이미지 목록 조회
     * 
     * @param productId 상품 ID
     * @param imageType 이미지 타입
     * @return 해당 타입의 이미지 목록
     */
    List<ProductImage> findByProductProductIdAndImageTypeOrderByImageOrderAsc(Long productId, ImageType imageType);
    
    /**
     * 상품의 활성 이미지만 조회
     * 
     * @param productId 상품 ID
     * @return 활성 이미지 목록
     */
    List<ProductImage> findByProductProductIdAndIsActiveTrueOrderByImageOrderAsc(Long productId);
    
    /**
     * 상품의 특정 타입의 활성 이미지 조회
     * 
     * @param productId 상품 ID
     * @param imageType 이미지 타입
     * @return 해당 타입의 활성 이미지 목록
     */
    List<ProductImage> findByProductProductIdAndImageTypeAndIsActiveTrueOrderByImageOrderAsc(
            Long productId, ImageType imageType);
    
    // === 대표 이미지 조회 ===
    
    /**
     * 상품의 첫 번째 썸네일 이미지 조회
     * 
     * @param productId 상품 ID
     * @return 첫 번째 썸네일 이미지 Optional
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.productId = :productId " +
           "AND pi.imageType = 'THUMBNAIL' AND pi.isActive = true " +
           "ORDER BY pi.imageOrder ASC LIMIT 1")
    Optional<ProductImage> findFirstThumbnailByProductId(@Param("productId") Long productId);
    
    /**
     * 상품의 대표 이미지 조회 (썸네일 우선, 없으면 첫 번째 활성 이미지)
     * 
     * @param productId 상품 ID
     * @return 대표 이미지 Optional
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.productId = :productId " +
           "AND pi.isActive = true " +
           "ORDER BY CASE WHEN pi.imageType = 'THUMBNAIL' THEN 1 ELSE 2 END, pi.imageOrder ASC " +
           "LIMIT 1")
    Optional<ProductImage> findPrimaryImageByProductId(@Param("productId") Long productId);
    
    // === 이미지 타입별 조회 ===
    
    /**
     * 이미지 타입별 조회
     * 
     * @param imageType 이미지 타입
     * @return 해당 타입의 이미지 목록
     */
    List<ProductImage> findByImageTypeOrderByImageOrderAsc(ImageType imageType);
    
    /**
     * 특정 타입의 활성 이미지 조회
     * 
     * @param imageType 이미지 타입
     * @return 해당 타입의 활성 이미지 목록
     */
    List<ProductImage> findByImageTypeAndIsActiveTrueOrderByImageOrderAsc(ImageType imageType);
    
    // === 이미지 개수 조회 ===
    
    /**
     * 상품의 이미지 개수 조회
     * 
     * @param productId 상품 ID
     * @return 이미지 개수
     */
    long countByProductProductId(Long productId);
    
    /**
     * 상품의 활성 이미지 개수 조회
     * 
     * @param productId 상품 ID
     * @return 활성 이미지 개수
     */
    long countByProductProductIdAndIsActiveTrue(Long productId);
    
    /**
     * 상품의 특정 타입 이미지 개수 조회
     * 
     * @param productId 상품 ID
     * @param imageType 이미지 타입
     * @return 해당 타입 이미지 개수
     */
    long countByProductProductIdAndImageType(Long productId, ImageType imageType);
    
    // === 이미지 순서 관리 ===
    
    /**
     * 상품 이미지의 최대 순서 번호 조회
     * 
     * @param productId 상품 ID
     * @return 최대 순서 번호 (없으면 0)
     */
    @Query("SELECT COALESCE(MAX(pi.imageOrder), 0) FROM ProductImage pi WHERE pi.product.productId = :productId")
    Integer findMaxImageOrderByProductId(@Param("productId") Long productId);
    
    /**
     * 특정 순서 이후의 이미지들 조회 (순서 재정렬용)
     * 
     * @param productId 상품 ID
     * @param fromOrder 기준 순서
     * @return 해당 순서 이후의 이미지 목록
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.productId = :productId " +
           "AND pi.imageOrder > :fromOrder ORDER BY pi.imageOrder ASC")
    List<ProductImage> findByProductIdAndOrderGreaterThan(@Param("productId") Long productId, 
                                                         @Param("fromOrder") Integer fromOrder);
    
    // === 이미지 상태별 조회 ===
    
    /**
     * 활성 이미지 조회
     * 
     * @return 모든 활성 이미지 목록
     */
    List<ProductImage> findByIsActiveTrueOrderByProductProductIdAscImageOrderAsc();
    
    /**
     * 비활성 이미지 조회
     * 
     * @return 모든 비활성 이미지 목록
     */
    List<ProductImage> findByIsActiveFalseOrderByProductProductIdAscImageOrderAsc();
    
    // === 이미지 메타데이터 기반 조회 ===
    
    /**
     * 특정 파일 크기 범위의 이미지 조회
     * 
     * @param minSize 최소 파일 크기 (bytes)
     * @param maxSize 최대 파일 크기 (bytes)
     * @return 해당 범위의 이미지 목록
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.fileSize BETWEEN :minSize AND :maxSize")
    List<ProductImage> findByFileSizeRange(@Param("minSize") Long minSize, 
                                          @Param("maxSize") Long maxSize);
    
    /**
     * 특정 해상도 이상의 이미지 조회
     * 
     * @param minWidth 최소 너비
     * @param minHeight 최소 높이
     * @return 해당 해상도 이상의 이미지 목록
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.width >= :minWidth AND pi.height >= :minHeight")
    List<ProductImage> findByResolutionGreaterThan(@Param("minWidth") Integer minWidth, 
                                                  @Param("minHeight") Integer minHeight);
    
    /**
     * 대용량 이미지 조회 (임계값 이상)
     * 
     * @param sizeThreshold 파일 크기 임계값 (bytes)
     * @return 대용량 이미지 목록
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.fileSize > :sizeThreshold ORDER BY pi.fileSize DESC")
    List<ProductImage> findLargeImages(@Param("sizeThreshold") Long sizeThreshold);
    
    // === 통계 및 집계 ===
    
    /**
     * 이미지 타입별 개수 통계
     * 
     * @return 이미지 타입별 개수 (Object[] 형태: [imageType, count])
     */
    @Query("SELECT pi.imageType, COUNT(pi) FROM ProductImage pi GROUP BY pi.imageType")
    List<Object[]> countByImageType();
    
    /**
     * 활성 이미지 타입별 개수 통계
     * 
     * @return 활성 이미지 타입별 개수
     */
    @Query("SELECT pi.imageType, COUNT(pi) FROM ProductImage pi WHERE pi.isActive = true GROUP BY pi.imageType")
    List<Object[]> countActiveImagesByType();
    
    /**
     * 전체 이미지 파일 크기 합계
     * 
     * @return 전체 이미지 파일 크기 합계 (bytes)
     */
    @Query("SELECT COALESCE(SUM(pi.fileSize), 0) FROM ProductImage pi WHERE pi.fileSize IS NOT NULL")
    Long getTotalFileSize();
    
    /**
     * 평균 이미지 파일 크기
     * 
     * @return 평균 이미지 파일 크기 (bytes)
     */
    @Query("SELECT COALESCE(AVG(pi.fileSize), 0) FROM ProductImage pi WHERE pi.fileSize IS NOT NULL")
    Double getAverageFileSize();
    
    // === 특정 조건 조회 ===
    
    /**
     * Alt 텍스트가 없는 이미지 조회 (접근성 개선용)
     * 
     * @return Alt 텍스트가 없는 이미지 목록
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.altText IS NULL OR pi.altText = ''")
    List<ProductImage> findImagesWithoutAltText();
    
    /**
     * 메타데이터가 없는 이미지 조회 (파일 크기, 너비, 높이가 모두 없는 경우)
     * 
     * @return 메타데이터가 없는 이미지 목록
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.fileSize IS NULL AND pi.width IS NULL AND pi.height IS NULL")
    List<ProductImage> findImagesWithoutMetadata();
    
    // === JOIN 조회 (Product와 함께) ===
    
    /**
     * 특정 카테고리의 이미지 조회
     * 
     * @param categoryId 카테고리 ID
     * @return 해당 카테고리의 이미지 목록
     */
    @Query("SELECT pi FROM ProductImage pi JOIN pi.product p WHERE p.category.categoryId = :categoryId " +
           "ORDER BY p.productId ASC, pi.imageOrder ASC")
    List<ProductImage> findByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * 특정 브랜드의 이미지 조회
     * 
     * @param brandId 브랜드 ID
     * @return 해당 브랜드의 이미지 목록
     */
    @Query("SELECT pi FROM ProductImage pi JOIN pi.product p WHERE p.brand.brandId = :brandId " +
           "ORDER BY p.productId ASC, pi.imageOrder ASC")
    List<ProductImage> findByBrandId(@Param("brandId") Long brandId);
    
    /**
     * 활성 상품의 이미지만 조회
     * 
     * @return 활성 상품의 이미지 목록
     */
    @Query("SELECT pi FROM ProductImage pi JOIN pi.product p WHERE p.status = 'ACTIVE' " +
           "ORDER BY p.productId ASC, pi.imageOrder ASC")
    List<ProductImage> findActiveProductImages();
}