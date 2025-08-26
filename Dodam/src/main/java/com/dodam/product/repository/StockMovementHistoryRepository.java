package com.dodam.product.repository;

import com.dodam.product.entity.StockMovementHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 재고 이동 이력 Repository 인터페이스
 * 
 * <p>재고 이동 이력의 데이터베이스 액세스를 담당합니다.</p>
 * <p>재고 변동 추적 및 이력 조회 기능을 제공합니다.</p>
 * 
 * @since 1.0.0
 */
@Repository
public interface StockMovementHistoryRepository extends JpaRepository<StockMovementHistory, Long> {
    
    // === 기본 조회 메서드 ===
    
    /**
     * 상품 ID로 재고 이동 이력을 최신 순으로 조회
     * 
     * @param productId 상품 ID
     * @return 재고 이동 이력 목록 (최신 순)
     */
    List<StockMovementHistory> findByProductIdOrderByCreatedAtDesc(Long productId);
    
    /**
     * 상품 ID로 재고 이동 이력을 페이지네이션으로 조회
     * 
     * @param productId 상품 ID
     * @param pageable 페이지네이션 정보
     * @return 재고 이동 이력 페이지 (최신 순)
     */
    Page<StockMovementHistory> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    
    /**
     * 창고 ID로 재고 이동 이력을 최신 순으로 조회
     * 
     * @param warehouseId 창고 ID
     * @return 재고 이동 이력 목록 (최신 순)
     */
    List<StockMovementHistory> findByWarehouseIdOrderByCreatedAtDesc(Long warehouseId);
    
    /**
     * 상품 ID와 창고 ID로 재고 이동 이력을 최신 순으로 조회
     * 
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @return 재고 이동 이력 목록 (최신 순)
     */
    List<StockMovementHistory> findByProductIdAndWarehouseIdOrderByCreatedAtDesc(Long productId, Long warehouseId);
    
    /**
     * 변경 유형으로 재고 이동 이력을 최신 순으로 조회
     * 
     * @param changeType 변경 유형
     * @return 재고 이동 이력 목록 (최신 순)
     */
    List<StockMovementHistory> findByChangeTypeOrderByCreatedAtDesc(StockMovementHistory.MovementType changeType);
    
    /**
     * 요청자로 재고 이동 이력을 최신 순으로 조회
     * 
     * @param requestedBy 요청자
     * @return 재고 이동 이력 목록 (최신 순)
     */
    List<StockMovementHistory> findByRequestedByOrderByCreatedAtDesc(String requestedBy);
    
    /**
     * 참조 번호로 재고 이동 이력을 조회
     * 
     * @param referenceNumber 참조 번호
     * @return 재고 이동 이력 목록
     */
    List<StockMovementHistory> findByReferenceNumber(String referenceNumber);
    
    // === 기간별 조회 메서드 ===
    
    /**
     * 상품 ID와 기간으로 재고 이동 이력을 조회
     * 
     * @param productId 상품 ID
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 재고 이동 이력 목록 (최신 순)
     */
    @Query("SELECT h FROM StockMovementHistory h " +
           "WHERE h.productId = :productId AND h.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY h.createdAt DESC")
    List<StockMovementHistory> findByProductIdAndDateRange(@Param("productId") Long productId,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * 창고 ID와 기간으로 재고 이동 이력을 조회
     * 
     * @param warehouseId 창고 ID
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 재고 이동 이력 목록 (최신 순)
     */
    @Query("SELECT h FROM StockMovementHistory h " +
           "WHERE h.warehouseId = :warehouseId AND h.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY h.createdAt DESC")
    List<StockMovementHistory> findByWarehouseIdAndDateRange(@Param("warehouseId") Long warehouseId,
                                                             @Param("startDate") LocalDateTime startDate,
                                                             @Param("endDate") LocalDateTime endDate);
    
    /**
     * 기간으로 재고 이동 이력을 조회
     * 
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 재고 이동 이력 목록 (최신 순)
     */
    @Query("SELECT h FROM StockMovementHistory h " +
           "WHERE h.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY h.createdAt DESC")
    List<StockMovementHistory> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    // === 복합 조건 조회 메서드 ===
    
    /**
     * 상품 ID와 변경 유형으로 재고 이동 이력을 조회
     * 
     * @param productId 상품 ID
     * @param changeType 변경 유형
     * @return 재고 이동 이력 목록 (최신 순)
     */
    List<StockMovementHistory> findByProductIdAndChangeTypeOrderByCreatedAtDesc(Long productId,
                                                                                StockMovementHistory.MovementType changeType);
    
    /**
     * 창고 ID와 변경 유형으로 재고 이동 이력을 조회
     * 
     * @param warehouseId 창고 ID
     * @param changeType 변경 유형
     * @return 재고 이동 이력 목록 (최신 순)
     */
    List<StockMovementHistory> findByWarehouseIdAndChangeTypeOrderByCreatedAtDesc(Long warehouseId,
                                                                                  StockMovementHistory.MovementType changeType);
    
    /**
     * 상품 ID, 변경 유형, 기간으로 재고 이동 이력을 조회
     * 
     * @param productId 상품 ID
     * @param changeType 변경 유형
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 재고 이동 이력 목록 (최신 순)
     */
    @Query("SELECT h FROM StockMovementHistory h " +
           "WHERE h.productId = :productId AND h.changeType = :changeType " +
           "AND h.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY h.createdAt DESC")
    List<StockMovementHistory> findByProductIdAndChangeTypeAndDateRange(@Param("productId") Long productId,
                                                                        @Param("changeType") StockMovementHistory.MovementType changeType,
                                                                        @Param("startDate") LocalDateTime startDate,
                                                                        @Param("endDate") LocalDateTime endDate);
    
    // === 통계 및 집계 조회 ===
    
    /**
     * 상품별 총 입고 수량 계산
     * 
     * @param productId 상품 ID
     * @param startDate 시작일시 (optional)
     * @param endDate 종료일시 (optional)
     * @return 총 입고 수량
     */
    @Query("SELECT COALESCE(SUM(h.changeQuantity), 0) FROM StockMovementHistory h " +
           "WHERE h.productId = :productId " +
           "AND h.changeType IN ('INCREASE', 'MOVE_IN', 'CANCEL_RESERVATION', 'RETURNED') " +
           "AND (:startDate IS NULL OR h.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR h.createdAt <= :endDate)")
    Long getTotalInflowQuantity(@Param("productId") Long productId,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * 상품별 총 출고 수량 계산
     * 
     * @param productId 상품 ID
     * @param startDate 시작일시 (optional)
     * @param endDate 종료일시 (optional)
     * @return 총 출고 수량
     */
    @Query("SELECT COALESCE(SUM(ABS(h.changeQuantity)), 0) FROM StockMovementHistory h " +
           "WHERE h.productId = :productId " +
           "AND h.changeType IN ('DECREASE', 'MOVE_OUT', 'RESERVE', 'CONFIRM_RESERVATION', 'EXPIRED', 'DAMAGED') " +
           "AND (:startDate IS NULL OR h.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR h.createdAt <= :endDate)")
    Long getTotalOutflowQuantity(@Param("productId") Long productId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * 창고별 재고 이동 통계 (입고/출고 건수)
     * 
     * @param warehouseId 창고 ID
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return [입고건수, 출고건수] 배열
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN h.changeType IN ('INCREASE', 'MOVE_IN', 'CANCEL_RESERVATION', 'RETURNED') THEN 1 END), " +
           "COUNT(CASE WHEN h.changeType IN ('DECREASE', 'MOVE_OUT', 'RESERVE', 'CONFIRM_RESERVATION', 'EXPIRED', 'DAMAGED') THEN 1 END) " +
           "FROM StockMovementHistory h " +
           "WHERE h.warehouseId = :warehouseId " +
           "AND h.createdAt BETWEEN :startDate AND :endDate")
    Object[] getWarehouseMovementStats(@Param("warehouseId") Long warehouseId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * 상품별 최근 재고 이동 이력 조회 (제한된 개수)
     * 
     * @param productId 상품 ID
     * @param limit 조회 제한 개수
     * @return 최근 재고 이동 이력 목록
     */
    @Query(value = "SELECT * FROM stock_movement_history " +
                   "WHERE product_id = :productId " +
                   "ORDER BY created_at DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<StockMovementHistory> findRecentMovementHistory(@Param("productId") Long productId,
                                                         @Param("limit") int limit);
    
    /**
     * 변경 유형별 이력 개수 조회
     * 
     * @param changeType 변경 유형
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 이력 개수
     */
    @Query("SELECT COUNT(h) FROM StockMovementHistory h " +
           "WHERE h.changeType = :changeType " +
           "AND h.createdAt BETWEEN :startDate AND :endDate")
    Long countByChangeTypeAndDateRange(@Param("changeType") StockMovementHistory.MovementType changeType,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * 요청자별 재고 이동 건수 조회
     * 
     * @param requestedBy 요청자
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 재고 이동 건수
     */
    @Query("SELECT COUNT(h) FROM StockMovementHistory h " +
           "WHERE h.requestedBy = :requestedBy " +
           "AND h.createdAt BETWEEN :startDate AND :endDate")
    Long countByRequestedByAndDateRange(@Param("requestedBy") String requestedBy,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
    
    // === 특수 조회 메서드 ===
    
    /**
     * 대량 재고 이동 이력 조회 (특정 수량 이상)
     * 
     * @param minQuantity 최소 수량
     * @return 대량 재고 이동 이력 목록 (최신 순)
     */
    @Query("SELECT h FROM StockMovementHistory h " +
           "WHERE ABS(h.changeQuantity) >= :minQuantity " +
           "ORDER BY h.createdAt DESC")
    List<StockMovementHistory> findLargeMovements(@Param("minQuantity") Integer minQuantity);
    
    /**
     * 특정 기간 동안 가장 활발한 재고 이동이 있었던 상품들 조회
     * 
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @param limit 조회 제한 개수
     * @return [상품ID, 이동건수] 목록
     */
    @Query("SELECT h.productId, COUNT(h) as movementCount " +
           "FROM StockMovementHistory h " +
           "WHERE h.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY h.productId " +
           "ORDER BY movementCount DESC")
    List<Object[]> findMostActiveProducts(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);
    
    /**
     * 재고 조정 이력만 조회
     * 
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 재고 조정 이력 목록 (최신 순)
     */
    @Query("SELECT h FROM StockMovementHistory h " +
           "WHERE h.changeType = 'ADJUSTMENT' " +
           "AND h.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY h.createdAt DESC")
    List<StockMovementHistory> findStockAdjustments(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
    
    /**
     * 손실 관련 이력 조회 (폐기, 파손)
     * 
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 손실 관련 이력 목록 (최신 순)
     */
    @Query("SELECT h FROM StockMovementHistory h " +
           "WHERE h.changeType IN ('EXPIRED', 'DAMAGED') " +
           "AND h.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY h.createdAt DESC")
    List<StockMovementHistory> findLossMovements(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
}