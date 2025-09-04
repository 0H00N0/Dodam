package com.dodam.product.repository;

import com.dodam.product.entity.ProductOption;
import com.dodam.product.common.enums.OptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * 상품 옵션 Repository 인터페이스
 * 
 * <p>상품 옵션 엔티티의 데이터베이스 액세스를 담당합니다.</p>
 * 
 * @since 1.0.0
 */
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    
    // === 기본 조회 메서드 ===
    
    /**
     * 상품별 옵션 조회 (표시 순서대로 정렬)
     * 
     * @param productId 상품 ID
     * @return 상품 옵션 목록
     */
    List<ProductOption> findByProductProductIdOrderByDisplayOrderAsc(Long productId);
    
    /**
     * 상품별 활성화된 옵션만 조회
     * 
     * @param productId 상품 ID
     * @return 활성 상품 옵션 목록
     */
    List<ProductOption> findByProductProductIdAndIsAvailableTrueOrderByDisplayOrderAsc(Long productId);
    
    /**
     * 상품의 특정 타입 옵션 조회
     * 
     * @param productId 상품 ID
     * @param optionType 옵션 타입
     * @return 상품 옵션 목록
     */
    List<ProductOption> findByProductProductIdAndOptionTypeOrderByDisplayOrderAsc(Long productId, OptionType optionType);
    
    /**
     * 상품의 특정 타입 활성 옵션 조회
     * 
     * @param productId 상품 ID
     * @param optionType 옵션 타입
     * @return 활성 상품 옵션 목록
     */
    List<ProductOption> findByProductProductIdAndOptionTypeAndIsAvailableTrueOrderByDisplayOrderAsc(
        Long productId, OptionType optionType);
    
    // === 특정 옵션 조회 ===
    
    /**
     * 상품의 옵션명과 옵션값으로 조회
     * 
     * @param productId 상품 ID
     * @param optionName 옵션명
     * @param optionValue 옵션값
     * @return 상품 옵션 Optional
     */
    Optional<ProductOption> findByProductProductIdAndOptionNameAndOptionValue(
        Long productId, String optionName, String optionValue);
    
    /**
     * 옵션 중복 체크
     * 
     * @param productId 상품 ID
     * @param optionType 옵션 타입
     * @param optionValue 옵션값
     * @return 존재 여부
     */
    boolean existsByProductProductIdAndOptionTypeAndOptionValue(
        Long productId, OptionType optionType, String optionValue);
    
    /**
     * 특정 옵션을 제외하고 중복 체크
     * 
     * @param productId 상품 ID
     * @param optionType 옵션 타입
     * @param optionValue 옵션값
     * @param excludeId 제외할 옵션 ID
     * @return 존재 여부
     */
    @Query("SELECT COUNT(po) > 0 FROM ProductOption po WHERE " +
           "po.product.productId = :productId AND po.optionType = :optionType AND " +
           "po.optionValue = :optionValue AND po.optionId != :excludeId")
    boolean existsByProductAndTypeAndValueExcludingId(
        @Param("productId") Long productId,
        @Param("optionType") OptionType optionType,
        @Param("optionValue") String optionValue,
        @Param("excludeId") Long excludeId);
    
    // === 재고 관련 조회 ===
    
    /**
     * 재고가 있는 옵션만 조회
     * 
     * @param productId 상품 ID
     * @return 재고가 있는 옵션 목록
     */
    @Query("SELECT po FROM ProductOption po WHERE po.product.productId = :productId " +
           "AND po.isAvailable = true AND (po.stockQuantity IS NULL OR po.stockQuantity > 0) " +
           "ORDER BY po.displayOrder ASC")
    List<ProductOption> findAvailableOptions(@Param("productId") Long productId);
    
    /**
     * 재고가 부족한 옵션 조회
     * 
     * @param productId 상품 ID
     * @param threshold 재고 임계값
     * @return 재고 부족 옵션 목록
     */
    @Query("SELECT po FROM ProductOption po WHERE po.product.productId = :productId " +
           "AND po.stockQuantity IS NOT NULL AND po.stockQuantity <= :threshold " +
           "ORDER BY po.stockQuantity ASC")
    List<ProductOption> findLowStockOptions(@Param("productId") Long productId, 
                                           @Param("threshold") Integer threshold);
    
    /**
     * 품절 옵션 조회
     * 
     * @param productId 상품 ID
     * @return 품절 옵션 목록
     */
    @Query("SELECT po FROM ProductOption po WHERE po.product.productId = :productId " +
           "AND po.stockQuantity IS NOT NULL AND po.stockQuantity = 0 " +
           "ORDER BY po.displayOrder ASC")
    List<ProductOption> findOutOfStockOptions(@Param("productId") Long productId);
    
    // === 통계 및 집계 ===
    
    /**
     * 상품별 옵션 개수 조회
     * 
     * @param productId 상품 ID
     * @return 옵션 개수
     */
    long countByProductProductId(Long productId);
    
    /**
     * 상품별 활성 옵션 개수 조회
     * 
     * @param productId 상품 ID
     * @return 활성 옵션 개수
     */
    long countByProductProductIdAndIsAvailableTrue(Long productId);
    
    /**
     * 상품의 특정 타입 옵션 개수 조회
     * 
     * @param productId 상품 ID
     * @param optionType 옵션 타입
     * @return 옵션 개수
     */
    long countByProductProductIdAndOptionType(Long productId, OptionType optionType);
    
    /**
     * 상품별 총 재고 수량 조회 (옵션별 재고 합계)
     * 
     * @param productId 상품 ID
     * @return 총 재고 수량
     */
    @Query("SELECT COALESCE(SUM(po.stockQuantity), 0) FROM ProductOption po " +
           "WHERE po.product.productId = :productId AND po.stockQuantity IS NOT NULL")
    long getTotalStockQuantity(@Param("productId") Long productId);
    
    // === 정렬 및 순서 관리 ===
    
    /**
     * 같은 상품 내에서 최대 표시 순서 조회
     * 
     * @param productId 상품 ID
     * @return 최대 표시 순서
     */
    @Query("SELECT COALESCE(MAX(po.displayOrder), 0) FROM ProductOption po " +
           "WHERE po.product.productId = :productId")
    Integer findMaxDisplayOrderByProductId(@Param("productId") Long productId);
    
    /**
     * 같은 상품의 같은 타입 내에서 최대 표시 순서 조회
     * 
     * @param productId 상품 ID
     * @param optionType 옵션 타입
     * @return 최대 표시 순서
     */
    @Query("SELECT COALESCE(MAX(po.displayOrder), 0) FROM ProductOption po " +
           "WHERE po.product.productId = :productId AND po.optionType = :optionType")
    Integer findMaxDisplayOrderByProductIdAndType(@Param("productId") Long productId, 
                                                 @Param("optionType") OptionType optionType);
    
    // === 타입별 조회 ===
    
    /**
     * 옵션 타입별 전체 옵션 조회 (통계용)
     * 
     * @param optionType 옵션 타입
     * @return 해당 타입의 모든 옵션
     */
    List<ProductOption> findByOptionType(OptionType optionType);
    
    /**
     * 특정 타입의 고유한 옵션값들 조회 (필터 목록 생성용)
     * 
     * @param optionType 옵션 타입
     * @return 고유한 옵션값 목록
     */
    @Query("SELECT DISTINCT po.optionValue FROM ProductOption po " +
           "WHERE po.optionType = :optionType AND po.isAvailable = true " +
           "ORDER BY po.optionValue")
    List<String> findDistinctValuesByType(@Param("optionType") OptionType optionType);
    
    /**
     * 상품의 특정 타입 옵션값들 조회
     * 
     * @param productId 상품 ID
     * @param optionType 옵션 타입
     * @return 옵션값 목록
     */
    @Query("SELECT po.optionValue FROM ProductOption po " +
           "WHERE po.product.productId = :productId AND po.optionType = :optionType " +
           "AND po.isAvailable = true ORDER BY po.displayOrder")
    List<String> findOptionValuesByProductAndType(@Param("productId") Long productId, 
                                                 @Param("optionType") OptionType optionType);
}