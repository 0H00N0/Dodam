package com.dodam.product.repository;

import com.dodam.product.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * 재고 Repository 인터페이스
 * 
 * <p>재고 엔티티의 데이터베이스 액세스를 담당합니다.</p>
 * <p>동시성 제어를 위한 락 처리 메서드들을 포함합니다.</p>
 * 
 * @since 1.0.0
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    // === 기본 조회 메서드 ===
    
    /**
     * 상품 ID로 재고 조회
     * 
     * @param productId 상품 ID
     * @return 재고 Optional
     */
    Optional<Inventory> findByProductProductId(Long productId);
    
    /**
     * 상품 ID로 재고 조회 (간단한 방법)
     * 
     * @param productId 상품 ID
     * @return 재고 Optional
     */
    @Query("SELECT i FROM Inventory i WHERE i.product.productId = :productId")
    Optional<Inventory> findByProductId(@Param("productId") Long productId);
    
    /**
     * 여러 상품 ID로 재고를 조회
     * 
     * @param productIds 상품 ID 목록
     * @return 재고 목록
     */
    @Query("SELECT i FROM Inventory i WHERE i.product.productId IN :productIds")
    List<Inventory> findByProductIdIn(@Param("productIds") List<Long> productIds);
    
    // === 동시성 제어를 위한 락 메서드 ===
    
    /**
     * 비관적 락으로 재고 조회 (동시 접근 제어)
     * 
     * @param productId 상품 ID
     * @return 재고 Optional (락 적용됨)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.productId = :productId")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") Long productId);
    
    /**
     * 비관적 락으로 재고 ID로 조회
     * 
     * @param inventoryId 재고 ID
     * @return 재고 Optional (락 적용됨)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.inventoryId = :inventoryId")
    Optional<Inventory> findByIdWithLock(@Param("inventoryId") Long inventoryId);
    
    /**
     * 여러 상품의 재고를 동시에 락으로 조회
     * 
     * @param productIds 상품 ID 목록
     * @return 재고 목록 (락 적용됨)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.productId IN :productIds ORDER BY i.product.productId")
    List<Inventory> findByProductIdsWithLock(@Param("productIds") List<Long> productIds);
    
    // === 재고 상태 조회 ===
    
    /**
     * 품절 상품 조회 (사용 가능 재고가 0인 것)
     * 
     * @return 품절 재고 목록
     */
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= 0")
    List<Inventory> findOutOfStockInventories();
    
    /**
     * 재고 부족 상품 조회 (최소 재고 수준 이하)
     * 
     * @return 재고 부족 재고 목록
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.minStockLevel AND i.minStockLevel > 0")
    List<Inventory> findLowStockInventories();
    
    /**
     * 특정 임계값 이하의 재고 조회
     * 
     * @param threshold 임계값
     * @return 임계값 이하 재고 목록
     */
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= :threshold")
    List<Inventory> findInventoriesBelowThreshold(@Param("threshold") Integer threshold);
    
    /**
     * 재고 부족 상품 조회 (특정 임계값 이하)
     * 
     * @param threshold 임계값
     * @return 재고 부족 재고 목록
     */
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= :threshold")
    List<Inventory> findLowStockProducts(@Param("threshold") int threshold);
    
    /**
     * 예약된 재고가 있는 상품 조회
     * 
     * @return 예약 재고가 있는 재고 목록
     */
    @Query("SELECT i FROM Inventory i WHERE i.reservedQuantity > 0")
    List<Inventory> findInventoriesWithReservedStock();
    
    /**
     * 특정 기간 동안 재입고되지 않은 재고 조회
     * 
     * @param days 일수
     * @return 장기간 재입고되지 않은 재고 목록
     */
    @Query("SELECT i FROM Inventory i WHERE i.lastRestockedAt IS NULL OR " +
           "i.lastRestockedAt < CURRENT_TIMESTAMP - :days DAY")
    List<Inventory> findInventoriesNotRestockedForDays(@Param("days") int days);
    
    // === 통계 및 집계 조회 ===
    
    /**
     * 총 재고 가치 계산 (재고 수량 * 상품 가격)
     * 
     * @return 총 재고 가치
     */
    @Query("SELECT COALESCE(SUM(i.quantity * p.price), 0) FROM Inventory i JOIN i.product p")
    Long calculateTotalInventoryValue();
    
    /**
     * 총 사용 가능한 재고 수량
     * 
     * @return 총 사용 가능 재고 수량
     */
    @Query("SELECT COALESCE(SUM(i.availableQuantity), 0) FROM Inventory i")
    Long getTotalAvailableQuantity();
    
    /**
     * 총 예약된 재고 수량
     * 
     * @return 총 예약 재고 수량
     */
    @Query("SELECT COALESCE(SUM(i.reservedQuantity), 0) FROM Inventory i")
    Long getTotalReservedQuantity();
    
    /**
     * 카테고리별 재고 통계
     * 
     * @param categoryId 카테고리 ID
     * @return 카테고리별 총 재고 수량
     */
    @Query("SELECT COALESCE(SUM(i.availableQuantity), 0) FROM Inventory i " +
           "JOIN i.product p WHERE p.category.categoryId = :categoryId")
    Long getTotalQuantityByCategory(@Param("categoryId") Long categoryId);
    
    /**
     * 브랜드별 재고 통계
     * 
     * @param brandId 브랜드 ID
     * @return 브랜드별 총 재고 수량
     */
    @Query("SELECT COALESCE(SUM(i.availableQuantity), 0) FROM Inventory i " +
           "JOIN i.product p WHERE p.brand.brandId = :brandId")
    Long getTotalQuantityByBrand(@Param("brandId") Long brandId);
    
    // === 재고 상태별 개수 조회 ===
    
    /**
     * 품절 상품 개수
     * 
     * @return 품절 상품 개수
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.availableQuantity <= 0")
    long countOutOfStockProducts();
    
    /**
     * 재고 부족 상품 개수 (최소 재고 수준 이하)
     * 
     * @return 재고 부족 상품 개수
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantity <= i.minStockLevel AND i.minStockLevel > 0")
    long countLowStockProducts();
    
    /**
     * 충분한 재고 상품 개수 (최소 재고 수준 초과)
     * 
     * @return 충분한 재고 상품 개수
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantity > i.minStockLevel AND i.minStockLevel > 0")
    long countSufficientStockProducts();
    
    /**
     * 예약 재고가 있는 상품 개수
     * 
     * @return 예약 재고가 있는 상품 개수
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.reservedQuantity > 0")
    long countProductsWithReservedStock();
    
    // === 재고 변동 추적 ===
    
    /**
     * 최근 재입고된 재고 조회
     * 
     * @param days 최근 일수
     * @return 최근 재입고된 재고 목록
     */
    @Query("SELECT i FROM Inventory i WHERE i.lastRestockedAt >= CURRENT_TIMESTAMP - :days DAY " +
           "ORDER BY i.lastRestockedAt DESC")
    List<Inventory> findRecentlyRestockedInventories(@Param("days") int days);
    
    /**
     * 최근 업데이트된 재고 조회
     * 
     * @param days 최근 일수
     * @return 최근 업데이트된 재고 목록
     */
    @Query("SELECT i FROM Inventory i WHERE i.updatedAt >= CURRENT_TIMESTAMP - :days DAY " +
           "ORDER BY i.updatedAt DESC")
    List<Inventory> findRecentlyUpdatedInventories(@Param("days") int days);
    
    // === 특정 조건의 재고 조회 ===
    
    /**
     * 특정 재고 수량 범위의 상품 조회
     * 
     * @param minQuantity 최소 재고 수량
     * @param maxQuantity 최대 재고 수량
     * @return 해당 범위의 재고 목록
     */
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity BETWEEN :minQuantity AND :maxQuantity")
    List<Inventory> findByQuantityRange(@Param("minQuantity") Integer minQuantity, 
                                       @Param("maxQuantity") Integer maxQuantity);
    
    /**
     * 활성 상품의 재고만 조회
     * 
     * @return 활성 상품의 재고 목록
     */
    @Query("SELECT i FROM Inventory i JOIN i.product p WHERE p.status = 'ACTIVE'")
    List<Inventory> findActiveProductInventories();
    
    /**
     * 주문 가능한 재고 조회 (활성 상품이면서 재고가 있는 것)
     * 
     * @return 주문 가능한 재고 목록
     */
    @Query("SELECT i FROM Inventory i JOIN i.product p " +
           "WHERE p.status = 'ACTIVE' AND i.availableQuantity > 0")
    List<Inventory> findOrderableInventories();
}