package com.dodam.product.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 재고 이동 이력 엔티티
 * 
 * <p>재고 변동에 대한 모든 이력을 추적하여 재고 관리의 투명성과 추적성을 제공합니다.</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "STOCK_MOVEMENT_HISTORY", indexes = {
    @Index(name = "idx_stock_movement_product", columnList = "product_id"),
    @Index(name = "idx_stock_movement_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_stock_movement_created_at", columnList = "created_at")
})
public class StockMovementHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "warehouse_id")
    private Long warehouseId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 50)
    private MovementType changeType;
    
    @Column(name = "change_quantity", nullable = false)
    private Integer changeQuantity;
    
    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity;
    
    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "requested_by", length = 100)
    private String requestedBy;
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 재고 이동 유형
     */
    public enum MovementType {
        INCREASE("입고", "재고 증가"),
        DECREASE("출고", "재고 감소"),
        RESERVE("예약", "재고 예약"),
        CONFIRM_RESERVATION("예약확정", "예약 재고 확정"),
        CANCEL_RESERVATION("예약취소", "예약 재고 취소"),
        MOVE_OUT("이동출고", "창고 간 이동 출고"),
        MOVE_IN("이동입고", "창고 간 이동 입고"),
        ADJUSTMENT("재고조정", "재고 수량 조정"),
        EXPIRED("폐기", "유통기한 만료 등으로 폐기"),
        RETURNED("반품", "고객 반품 처리"),
        DAMAGED("파손", "상품 파손 처리");
        
        private final String displayName;
        private final String description;
        
        MovementType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 기본 생성자
     */
    public StockMovementHistory() {}
    
    /**
     * 재고 이동 이력 생성자
     * 
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param changeType 변경 유형
     * @param changeQuantity 변경 수량
     * @param previousQuantity 이전 수량
     * @param newQuantity 새로운 수량
     * @param reason 변경 사유
     * @param requestedBy 요청자
     * @param referenceNumber 참조 번호
     */
    public StockMovementHistory(Long productId, Long warehouseId, MovementType changeType, 
                               Integer changeQuantity, Integer previousQuantity, Integer newQuantity,
                               String reason, String requestedBy, String referenceNumber) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.changeType = changeType;
        this.changeQuantity = changeQuantity;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.reason = reason;
        this.requestedBy = requestedBy;
        this.referenceNumber = referenceNumber;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Builder 패턴을 위한 정적 팩토리 메서드
     */
    public static StockMovementHistoryBuilder builder() {
        return new StockMovementHistoryBuilder();
    }
    
    /**
     * 변경 수량의 절댓값을 반환합니다.
     * 
     * @return 변경 수량의 절댓값
     */
    public Integer getAbsoluteChangeQuantity() {
        return Math.abs(changeQuantity);
    }
    
    /**
     * 재고 증가인지 확인합니다.
     * 
     * @return 재고 증가 여부
     */
    public boolean isStockIncrease() {
        return changeQuantity > 0;
    }
    
    /**
     * 재고 감소인지 확인합니다.
     * 
     * @return 재고 감소 여부
     */
    public boolean isStockDecrease() {
        return changeQuantity < 0;
    }
    
    /**
     * 재고 이동 이력의 상세 정보를 반환합니다.
     * 
     * @return 상세 정보 문자열
     */
    public String getDetailedInfo() {
        return String.format("%s: %d개 (%d → %d) [%s]", 
            changeType.getDisplayName(), 
            getAbsoluteChangeQuantity(),
            previousQuantity, 
            newQuantity,
            reason != null ? reason : "사유 없음"
        );
    }
    
    // === Getters and Setters ===
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getWarehouseId() {
        return warehouseId;
    }
    
    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }
    
    public MovementType getChangeType() {
        return changeType;
    }
    
    public void setChangeType(MovementType changeType) {
        this.changeType = changeType;
    }
    
    public Integer getChangeQuantity() {
        return changeQuantity;
    }
    
    public void setChangeQuantity(Integer changeQuantity) {
        this.changeQuantity = changeQuantity;
    }
    
    public Integer getPreviousQuantity() {
        return previousQuantity;
    }
    
    public void setPreviousQuantity(Integer previousQuantity) {
        this.previousQuantity = previousQuantity;
    }
    
    public Integer getNewQuantity() {
        return newQuantity;
    }
    
    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getRequestedBy() {
        return requestedBy;
    }
    
    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }
    
    public String getReferenceNumber() {
        return referenceNumber;
    }
    
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // === Builder 클래스 ===
    
    public static class StockMovementHistoryBuilder {
        private Long productId;
        private Long warehouseId;
        private MovementType movementType;
        private Integer quantity;
        private Integer previousAvailableStock;
        private Integer previousReservedStock;
        private String reason;
        private String referenceNumber;
        private String requestedBy;
        
        public StockMovementHistoryBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }
        
        public StockMovementHistoryBuilder warehouseId(Long warehouseId) {
            this.warehouseId = warehouseId;
            return this;
        }
        
        public StockMovementHistoryBuilder movementType(MovementType movementType) {
            this.movementType = movementType;
            return this;
        }
        
        public StockMovementHistoryBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }
        
        public StockMovementHistoryBuilder previousAvailableStock(Integer previousAvailableStock) {
            this.previousAvailableStock = previousAvailableStock;
            return this;
        }
        
        public StockMovementHistoryBuilder previousReservedStock(Integer previousReservedStock) {
            this.previousReservedStock = previousReservedStock;
            return this;
        }
        
        public StockMovementHistoryBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        public StockMovementHistoryBuilder referenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
            return this;
        }
        
        public StockMovementHistoryBuilder requestedBy(String requestedBy) {
            this.requestedBy = requestedBy;
            return this;
        }
        
        public StockMovementHistoryBuilder createdAt(LocalDateTime createdAt) {
            // Builder에서는 createdAt를 자동 설정하므로 무시
            return this;
        }
        
        public StockMovementHistory build() {
            StockMovementHistory history = new StockMovementHistory();
            history.setProductId(this.productId);
            history.setWarehouseId(this.warehouseId);
            history.setChangeType(this.movementType);
            history.setChangeQuantity(this.quantity);
            history.setPreviousQuantity(this.previousAvailableStock != null ? this.previousAvailableStock : 0);
            history.setNewQuantity(calculateNewQuantity());
            history.setReason(this.reason);
            history.setReferenceNumber(this.referenceNumber);
            history.setRequestedBy(this.requestedBy);
            history.setCreatedAt(LocalDateTime.now());
            return history;
        }
        
        private Integer calculateNewQuantity() {
            if (previousAvailableStock == null) {
                return 0;
            }
            
            // 재고 이동 유형에 따라 새로운 수량 계산
            switch (movementType) {
                case INCREASE:
                case MOVE_IN:
                case CANCEL_RESERVATION:
                    return previousAvailableStock + Math.abs(quantity);
                case DECREASE:
                case MOVE_OUT:
                case RESERVE:
                case CONFIRM_RESERVATION:
                    return previousAvailableStock - Math.abs(quantity);
                default:
                    return previousAvailableStock;
            }
        }
    }
}