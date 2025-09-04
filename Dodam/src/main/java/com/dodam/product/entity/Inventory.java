package com.dodam.product.entity;

// import com.dodam.product.exception.InsufficientStockException; // Temporarily disabled
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 재고 엔티티
 * 
 * <p>상품의 재고 정보를 관리하며, 동시성 제어를 위한 낙관적 락을 적용합니다.</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "INVENTORY", indexes = {
    @Index(name = "idx_inventory_product", columnList = "product_id")
})
public class Inventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
    
    @Column(name = "min_stock_level")
    private Integer minStockLevel;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 기본 생성자 (테스트용 public 접근자)
     */
    public Inventory() {}
    
    /**
     * 재고 생성자
     * 
     * @param product 상품
     * @param initialQuantity 초기 재고 수량
     */
    public Inventory(Product product, Integer initialQuantity) {
        this.product = product;
        this.quantity = initialQuantity;
        this.reservedQuantity = 0;
        this.availableQuantity = initialQuantity;
        this.minStockLevel = 10; // 기본 최소 재고 수준
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 재고 생성자 (최소 재고 수준 포함)
     * 
     * @param product 상품
     * @param initialQuantity 초기 재고 수량
     * @param minStockLevel 최소 재고 수준
     */
    public Inventory(Product product, Integer initialQuantity, Integer minStockLevel) {
        this(product, initialQuantity);
        this.minStockLevel = minStockLevel;
    }
    
    // === 재고 관리 비즈니스 메서드 ===
    
    /**
     * 재고를 감소시킵니다 (예약 처리).
     * 
     * @param amount 감소할 수량
     * @throws RuntimeException 재고가 부족한 경우
     */
    public void decreaseStock(int amount) {
        if (availableQuantity < amount) {
            throw new RuntimeException(
                String.format("재고가 부족합니다. 요청수량: %d, 사용가능수량: %d", amount, availableQuantity)
            );
        }
        this.availableQuantity -= amount;
        this.reservedQuantity += amount;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 예약된 재고를 확정 처리합니다.
     * 
     * @param amount 확정할 수량
     */
    public void confirmReservedStock(int amount) {
        if (reservedQuantity < amount) {
            throw new IllegalStateException(
                String.format("예약 수량이 부족합니다. 요청수량: %d, 예약수량: %d", amount, reservedQuantity)
            );
        }
        this.quantity -= amount;
        this.reservedQuantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 예약된 재고를 취소합니다.
     * 
     * @param amount 취소할 수량
     */
    public void cancelReservedStock(int amount) {
        if (reservedQuantity < amount) {
            throw new IllegalStateException(
                String.format("예약 수량이 부족합니다. 요청수량: %d, 예약수량: %d", amount, reservedQuantity)
            );
        }
        this.availableQuantity += amount;
        this.reservedQuantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 재고를 입고합니다.
     * 
     * @param amount 입고할 수량
     */
    public void addStock(int amount) {
        this.quantity += amount;
        this.availableQuantity += amount;
        this.lastRestockedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 재고를 예약합니다.
     * 
     * @param amount 예약할 수량
     * @throws RuntimeException 사용 가능한 재고가 부족한 경우
     */
    public void reserveStock(int amount) {
        if (availableQuantity < amount) {
            throw new RuntimeException(
                String.format("재고가 부족합니다. 요청수량: %d, 사용가능수량: %d", amount, availableQuantity)
            );
        }
        this.availableQuantity -= amount;
        this.reservedQuantity += amount;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 예약된 재고를 확정합니다.
     * 
     * @param amount 확정할 수량
     */
    public void confirmReservation(int amount) {
        if (reservedQuantity < amount) {
            throw new IllegalStateException(
                String.format("예약 수량이 부족합니다. 요청수량: %d, 예약수량: %d", amount, reservedQuantity)
            );
        }
        this.quantity -= amount;
        this.reservedQuantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 예약을 취소합니다.
     * 
     * @param amount 취소할 수량
     */
    public void cancelReservation(int amount) {
        if (reservedQuantity < amount) {
            throw new IllegalStateException(
                String.format("예약 수량이 부족합니다. 요청수량: %d, 예약수량: %d", amount, reservedQuantity)
            );
        }
        this.availableQuantity += amount;
        this.reservedQuantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 사용 가능한 재고를 증가시킵니다.
     * 
     * @param amount 증가할 수량
     */
    public void increaseAvailableStock(int amount) {
        this.quantity += amount;
        this.availableQuantity += amount;
        this.lastRestockedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 사용 가능한 재고를 감소시킵니다.
     * 
     * @param amount 감소할 수량
     * @throws RuntimeException 사용 가능한 재고가 부족한 경우
     */
    public void decreaseAvailableStock(int amount) {
        if (availableQuantity < amount) {
            throw new RuntimeException(
                String.format("재고가 부족합니다. 요청수량: %d, 사용가능수량: %d", amount, availableQuantity)
            );
        }
        this.availableQuantity -= amount;
        this.quantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 재고 수량을 직접 설정합니다.
     * 
     * @param newQuantity 새로운 총 재고 수량
     */
    public void setStockQuantity(int newQuantity) {
        int difference = newQuantity - this.quantity;
        this.quantity = newQuantity;
        this.availableQuantity = Math.max(0, this.availableQuantity + difference);
        this.updatedAt = LocalDateTime.now();
        
        if (difference > 0) {
            this.lastRestockedAt = LocalDateTime.now();
        }
    }
    
    // === 재고 상태 확인 메서드 ===
    
    /**
     * 재고가 부족한지 확인합니다.
     * 
     * @return 재고 부족 여부
     */
    public boolean isLowStock() {
        return quantity <= minStockLevel;
    }
    
    /**
     * 재고가 없는지 확인합니다.
     * 
     * @return 재고 없음 여부
     */
    public boolean isOutOfStock() {
        return availableQuantity <= 0;
    }
    
    /**
     * 지정된 수량의 주문이 가능한지 확인합니다.
     * 
     * @param requestedQuantity 요청 수량
     * @return 주문 가능 여부
     */
    public boolean canFulfillOrder(int requestedQuantity) {
        return availableQuantity >= requestedQuantity;
    }
    
    /**
     * 재고 상태 정보를 반환합니다.
     * 
     * @return 재고 상태 문자열
     */
    public String getStockStatus() {
        if (isOutOfStock()) {
            return "품절";
        } else if (isLowStock()) {
            return "재고부족";
        } else {
            return "충분";
        }
    }
    
    // === Getters and Setters ===
    
    public Long getInventoryId() {
        return inventoryId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public Integer getReservedQuantity() {
        return reservedQuantity;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
    
    public Integer getMinStockLevel() {
        return minStockLevel;
    }
    
    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public LocalDateTime getLastRestockedAt() {
        return lastRestockedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // === 테스트용 setter 메서드들 ===
    
    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
    
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
    
    public void setLastRestockedAt(LocalDateTime lastRestockedAt) {
        this.lastRestockedAt = lastRestockedAt;
    }
}