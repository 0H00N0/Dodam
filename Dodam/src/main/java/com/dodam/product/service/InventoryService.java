package com.dodam.product.service;

import com.dodam.product.dto.request.InventoryRequest;
import com.dodam.product.dto.request.StockMovementRequest;
import com.dodam.product.dto.response.InventoryResponse;
import com.dodam.product.entity.StockMovementHistory;

import java.util.List;
import java.util.Optional;

/**
 * 재고 관리 서비스 인터페이스
 * 
 * 동시성 제어와 재고 일관성을 보장하는 재고 관리 기능을 제공합니다.
 */
public interface InventoryService {
    
    /**
     * 상품의 현재 재고를 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 재고 정보
     */
    Optional<InventoryResponse> getInventory(Long productId);
    
    /**
     * 재고를 감소시킵니다. (주문 시 사용)
     * 
     * @param request 재고 감소 요청
     * @return 업데이트된 재고 정보
     * @throws com.dodam.product.exception.InsufficientStockException 재고 부족시 예외 발생
     */
    InventoryResponse decreaseStock(InventoryRequest request);
    
    /**
     * 재고를 증가시킵니다. (입고, 반품 시 사용)
     * 
     * @param request 재고 증가 요청
     * @return 업데이트된 재고 정보
     */
    InventoryResponse increaseStock(InventoryRequest request);
    
    /**
     * 재고를 예약합니다. (장바구니, 주문 대기 시 사용)
     * 
     * @param request 재고 예약 요청
     * @return 업데이트된 재고 정보
     * @throws com.dodam.product.exception.InsufficientStockException 재고 부족시 예외 발생
     */
    InventoryResponse reserveStock(InventoryRequest request);
    
    /**
     * 예약된 재고를 확정합니다. (주문 완료 시 사용)
     * 
     * @param request 예약 확정 요청
     * @return 업데이트된 재고 정보
     */
    InventoryResponse confirmReservation(InventoryRequest request);
    
    /**
     * 예약된 재고를 취소합니다. (주문 취소, 장바구니 제거 시 사용)
     * 
     * @param request 예약 취소 요청
     * @return 업데이트된 재고 정보
     */
    InventoryResponse cancelReservation(InventoryRequest request);
    
    /**
     * 재고 이동을 수행합니다. (창고간 이동 시 사용)
     * 
     * @param request 재고 이동 요청
     * @return 업데이트된 재고 정보
     */
    InventoryResponse moveStock(StockMovementRequest request);
    
    /**
     * 재고 변경 이력을 조회합니다.
     * 
     * @param productId 상품 ID
     * @param limit 조회 개수 제한
     * @return 재고 변경 이력 목록
     */
    List<StockMovementHistory> getStockHistory(Long productId, int limit);
    
    /**
     * 재고 부족 상품 목록을 조회합니다.
     * 
     * @param threshold 기준 재고량 (이하 시 알림 대상)
     * @return 재고 부족 상품 목록
     */
    List<InventoryResponse> getLowStockProducts(int threshold);
    
    /**
     * 여러 상품의 재고를 일괄 조회합니다.
     * 
     * @param productIds 상품 ID 목록
     * @return 재고 정보 목록
     */
    List<InventoryResponse> getBulkInventory(List<Long> productIds);
    
    /**
     * 재고 일관성을 검증합니다.
     * 
     * @param productId 상품 ID
     * @return 검증 결과 (true: 일관성 유지, false: 불일치 발견)
     */
    boolean validateStockConsistency(Long productId);
}