package com.dodam.product.exception;

/**
 * 재고를 찾을 수 없을 때 발생하는 예외
 * 
 * <p>존재하지 않는 재고 정보에 접근하려 할 때 발생합니다.</p>
 * 
 * @since 1.0.0
 */
public class InventoryNotFoundException extends RuntimeException {
    
    /**
     * 기본 생성자
     */
    public InventoryNotFoundException() {
        super("재고 정보를 찾을 수 없습니다.");
    }
    
    /**
     * 메시지를 포함한 생성자
     * 
     * @param message 예외 메시지
     */
    public InventoryNotFoundException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * @param message 예외 메시지
     * @param cause 원인
     */
    public InventoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 상품 ID로 구체적인 에러 메시지를 생성하는 정적 팩토리 메서드
     * 
     * @param productId 상품 ID
     * @return InventoryNotFoundException 인스턴스
     */
    public static InventoryNotFoundException forProductId(Long productId) {
        return new InventoryNotFoundException(
            String.format("상품 ID %d에 대한 재고 정보를 찾을 수 없습니다.", productId)
        );
    }
    
    /**
     * 상품 ID와 창고 ID로 구체적인 에러 메시지를 생성하는 정적 팩토리 메서드
     * 
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @return InventoryNotFoundException 인스턴스
     */
    public static InventoryNotFoundException forProductAndWarehouse(Long productId, Long warehouseId) {
        return new InventoryNotFoundException(
            String.format("상품 ID %d, 창고 ID %d에 대한 재고 정보를 찾을 수 없습니다.", productId, warehouseId)
        );
    }
}