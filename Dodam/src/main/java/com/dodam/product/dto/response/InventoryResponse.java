package com.dodam.product.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 재고 현황 응답 DTO
 * 
 * 현재 재고 상태 정보를 제공합니다.
 */
@Getter
@AllArgsConstructor
@Builder
public class InventoryResponse {
    
    /**
     * 상품 ID
     */
    private Long productId;
    
    /**
     * 상품명
     */
    private String productName;
    
    /**
     * 현재 사용 가능한 재고량
     */
    private Integer availableStock;
    
    /**
     * 예약된 재고량
     */
    private Integer reservedStock;
    
    /**
     * 총 재고량 (사용가능 + 예약됨)
     */
    private Integer totalStock;
    
    /**
     * 최소 재고 기준량
     */
    private Integer minimumStock;
    
    /**
     * 창고 ID
     */
    private Long warehouseId;
    
    /**
     * 창고명
     */
    private String warehouseName;
    
    /**
     * 재고 상태 (정상, 부족, 과다)
     */
    private StockStatus status;
    
    /**
     * 마지막 재고 변경 일시
     */
    private LocalDateTime lastUpdated;
    
    /**
     * 버전 정보 (낙관적 락용)
     */
    private Long version;
    
    /**
     * 재고 상태 열거형
     */
    public enum StockStatus {
        NORMAL("정상"),
        LOW("부족"), 
        CRITICAL("심각부족"),
        OUT_OF_STOCK("재고없음"),
        OVERSTOCKED("과다재고");
        
        private final String description;
        
        StockStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 재고 부족 여부를 확인합니다.
     * 
     * @return 재고 부족 여부
     */
    public boolean isLowStock() {
        return availableStock <= minimumStock;
    }
    
    /**
     * 재고가 완전히 소진되었는지 확인합니다.
     * 
     * @return 재고 소진 여부
     */
    public boolean isOutOfStock() {
        return availableStock <= 0;
    }
    
    /**
     * 요청한 수량만큼 재고가 충분한지 확인합니다.
     * 
     * @param requestedQuantity 요청 수량
     * @return 재고 충분 여부
     */
    public boolean hasSufficientStock(Integer requestedQuantity) {
        return availableStock >= requestedQuantity;
    }
}