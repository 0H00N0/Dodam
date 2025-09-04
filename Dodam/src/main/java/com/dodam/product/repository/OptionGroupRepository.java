package com.dodam.product.repository;

import com.dodam.product.entity.OptionGroup;
import com.dodam.product.common.enums.OptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 옵션 그룹 Repository 인터페이스
 * 
 * <p>상품 옵션 그룹의 데이터베이스 액세스를 담당합니다.</p>
 * <p>옵션 그룹 조회, 정렬, 필터링 기능을 제공합니다.</p>
 * 
 * @since 1.0.0
 */
@Repository
public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {
    
    // === 기본 조회 메서드 ===
    
    /**
     * 상품 ID로 옵션 그룹을 정렬 순서대로 조회
     * 
     * @param productId 상품 ID
     * @return 옵션 그룹 목록 (정렬 순서)
     */
    List<OptionGroup> findByProductProductIdOrderBySortOrder(Long productId);
    
    /**
     * 상품 ID로 옵션 그룹을 조회
     * 
     * @param productId 상품 ID
     * @return 옵션 그룹 목록
     */
    List<OptionGroup> findByProductProductId(Long productId);
    
    /**
     * 상품 ID와 그룹명으로 옵션 그룹을 조회
     * 
     * @param productId 상품 ID
     * @param groupName 그룹명
     * @return 옵션 그룹 Optional
     */
    Optional<OptionGroup> findByProductProductIdAndGroupName(Long productId, String groupName);
    
    /**
     * 상품 ID와 옵션 유형으로 옵션 그룹을 조회
     * 
     * @param productId 상품 ID
     * @param optionType 옵션 유형
     * @return 옵션 그룹 목록
     */
    List<OptionGroup> findByProductProductIdAndOptionType(Long productId, OptionType optionType);
    
    /**
     * 옵션 유형으로 옵션 그룹을 조회
     * 
     * @param optionType 옵션 유형
     * @return 옵션 그룹 목록
     */
    List<OptionGroup> findByOptionType(OptionType optionType);
    
    // === 필수/선택 옵션 조회 ===
    
    /**
     * 상품의 필수 옵션 그룹을 조회
     * 
     * @param productId 상품 ID
     * @return 필수 옵션 그룹 목록 (정렬 순서)
     */
    List<OptionGroup> findByProductProductIdAndIsRequiredTrueOrderBySortOrder(Long productId);
    
    /**
     * 상품의 선택 옵션 그룹을 조회
     * 
     * @param productId 상품 ID
     * @return 선택 옵션 그룹 목록 (정렬 순서)
     */
    List<OptionGroup> findByProductProductIdAndIsRequiredFalseOrderBySortOrder(Long productId);
    
    /**
     * 필수 여부로 옵션 그룹을 조회
     * 
     * @param isRequired 필수 여부
     * @return 옵션 그룹 목록
     */
    List<OptionGroup> findByIsRequired(Boolean isRequired);
    
    // === 정렬 관련 조회 ===
    
    /**
     * 상품의 특정 정렬 순서 이후의 옵션 그룹을 조회
     * 
     * @param productId 상품 ID
     * @param sortOrder 정렬 순서
     * @return 옵션 그룹 목록
     */
    List<OptionGroup> findByProductProductIdAndSortOrderGreaterThanOrderBySortOrder(Long productId, Integer sortOrder);
    
    /**
     * 상품의 최대 정렬 순서를 조회
     * 
     * @param productId 상품 ID
     * @return 최대 정렬 순서
     */
    @Query("SELECT COALESCE(MAX(og.sortOrder), 0) FROM OptionGroup og WHERE og.product.productId = :productId")
    Integer findMaxSortOrderByProductId(@Param("productId") Long productId);
    
    // === 통계 조회 ===
    
    /**
     * 상품의 옵션 그룹 개수를 조회
     * 
     * @param productId 상품 ID
     * @return 옵션 그룹 개수
     */
    long countByProductProductId(Long productId);
    
    /**
     * 상품의 필수 옵션 그룹 개수를 조회
     * 
     * @param productId 상품 ID
     * @return 필수 옵션 그룹 개수
     */
    long countByProductProductIdAndIsRequiredTrue(Long productId);
    
    /**
     * 상품의 선택 옵션 그룹 개수를 조회
     * 
     * @param productId 상품 ID
     * @return 선택 옵션 그룹 개수
     */
    long countByProductProductIdAndIsRequiredFalse(Long productId);
    
    /**
     * 옵션 유형별 그룹 개수를 조회
     * 
     * @param optionType 옵션 유형
     * @return 옵션 그룹 개수
     */
    long countByOptionType(OptionType optionType);
    
    // === 복합 조건 조회 ===
    
    /**
     * 상품 ID와 필수 여부, 옵션 유형으로 조회
     * 
     * @param productId 상품 ID
     * @param isRequired 필수 여부
     * @param optionType 옵션 유형
     * @return 옵션 그룹 목록 (정렬 순서)
     */
    List<OptionGroup> findByProductProductIdAndIsRequiredAndOptionTypeOrderBySortOrder(
            Long productId, Boolean isRequired, OptionType optionType);
    
    // === 활성 옵션이 있는 그룹 조회 ===
    
    /**
     * 활성 옵션이 있는 옵션 그룹만 조회
     * 
     * @param productId 상품 ID
     * @return 활성 옵션이 있는 옵션 그룹 목록
     */
    @Query("SELECT DISTINCT og FROM OptionGroup og " +
           "JOIN og.productOptions po " +
           "WHERE og.product.productId = :productId AND po.isAvailable = true " +
           "ORDER BY og.sortOrder")
    List<OptionGroup> findByProductIdWithActiveOptions(@Param("productId") Long productId);
    
    /**
     * 비활성 옵션만 있는 옵션 그룹 조회
     * 
     * @param productId 상품 ID
     * @return 비활성 옵션만 있는 옵션 그룹 목록
     */
    @Query("SELECT og FROM OptionGroup og " +
           "WHERE og.product.productId = :productId " +
           "AND NOT EXISTS (SELECT po FROM ProductOption po " +
                           "WHERE po.optionGroup = og AND po.isAvailable = true) " +
           "ORDER BY og.sortOrder")
    List<OptionGroup> findByProductIdWithInactiveOptionsOnly(@Param("productId") Long productId);
    
    // === 특정 조건의 옵션 그룹 존재 여부 ===
    
    /**
     * 상품에 특정 그룹명의 옵션 그룹이 존재하는지 확인
     * 
     * @param productId 상품 ID
     * @param groupName 그룹명
     * @return 존재 여부
     */
    boolean existsByProductProductIdAndGroupName(Long productId, String groupName);
    
    /**
     * 상품에 특정 옵션 유형의 그룹이 존재하는지 확인
     * 
     * @param productId 상품 ID
     * @param optionType 옵션 유형
     * @return 존재 여부
     */
    boolean existsByProductProductIdAndOptionType(Long productId, OptionType optionType);
    
    /**
     * 상품에 필수 옵션 그룹이 존재하는지 확인
     * 
     * @param productId 상품 ID
     * @return 필수 옵션 그룹 존재 여부
     */
    boolean existsByProductProductIdAndIsRequiredTrue(Long productId);
    
    // === 삭제 관련 메서드 ===
    
    /**
     * 상품의 모든 옵션 그룹을 삭제
     * 
     * @param productId 상품 ID
     * @return 삭제된 개수
     */
    long deleteByProductProductId(Long productId);
    
    /**
     * 상품의 특정 옵션 유형 그룹을 삭제
     * 
     * @param productId 상품 ID
     * @param optionType 옵션 유형
     * @return 삭제된 개수
     */
    long deleteByProductProductIdAndOptionType(Long productId, OptionType optionType);
    
    // === 옵션 개수 포함 조회 ===
    
    /**
     * 상품의 옵션 그룹과 각 그룹의 옵션 개수를 함께 조회
     * 
     * @param productId 상품 ID
     * @return [OptionGroup, 옵션개수] 목록
     */
    @Query("SELECT og, COUNT(po) FROM OptionGroup og " +
           "LEFT JOIN og.productOptions po " +
           "WHERE og.product.productId = :productId " +
           "GROUP BY og " +
           "ORDER BY og.sortOrder")
    List<Object[]> findByProductIdWithOptionCount(@Param("productId") Long productId);
    
    /**
     * 상품의 옵션 그룹과 활성 옵션 개수를 함께 조회
     * 
     * @param productId 상품 ID
     * @return [OptionGroup, 활성옵션개수] 목록
     */
    @Query("SELECT og, COUNT(CASE WHEN po.isAvailable = true THEN 1 END) FROM OptionGroup og " +
           "LEFT JOIN og.productOptions po " +
           "WHERE og.product.productId = :productId " +
           "GROUP BY og " +
           "ORDER BY og.sortOrder")
    List<Object[]> findByProductIdWithActiveOptionCount(@Param("productId") Long productId);
    
    // === 범위 조회 ===
    
    /**
     * 여러 상품의 옵션 그룹을 한번에 조회
     * 
     * @param productIds 상품 ID 목록
     * @return 옵션 그룹 목록 (상품별, 정렬순서별)
     */
    @Query("SELECT og FROM OptionGroup og " +
           "WHERE og.product.productId IN :productIds " +
           "ORDER BY og.product.productId, og.sortOrder")
    List<OptionGroup> findByProductProductIdIn(@Param("productIds") List<Long> productIds);
}