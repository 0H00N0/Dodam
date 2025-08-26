package com.dodam.product.exception;

/**
 * 재고가 부족할 때 발생하는 예외
 * 
 * @since 1.0.0
 */
public class InsufficientStockException extends RuntimeException {
    
    private static final String DEFAULT_MESSAGE = "재고가 부족합니다.";
    
    /**
     * 기본 생성자
     */
    public InsufficientStockException() {
        super(DEFAULT_MESSAGE);
    }
    
    /**
     * 메시지를 포함한 생성자
     * 
     * @param message 예외 메시지
     */
    public InsufficientStockException(String message) {
        super(message);
    }
    
    /**
     * 상품 ID와 요청 수량을 포함한 생성자
     * 
     * @param productId 상품 ID
     * @param requestedQuantity 요청 수량
     * @param availableQuantity 사용 가능 수량
     */
    public InsufficientStockException(Long productId, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format(
            "재고가 부족합니다. 상품 ID: %d, 요청 수량: %d, 사용가능 수량: %d", 
            productId, requestedQuantity, availableQuantity
        ));
    }
    
    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * @param message 예외 메시지
     * @param cause 예외 원인
     */
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}