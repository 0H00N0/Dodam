package com.dodam.product.exception;

/**
 * 상품을 찾을 수 없을 때 발생하는 예외
 * 
 * @since 1.0.0
 */
public class ProductNotFoundException extends RuntimeException {
    
    private static final String DEFAULT_MESSAGE = "상품을 찾을 수 없습니다.";
    
    /**
     * 기본 생성자
     */
    public ProductNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    /**
     * 메시지를 포함한 생성자
     * 
     * @param message 예외 메시지
     */
    public ProductNotFoundException(String message) {
        super(message);
    }
    
    /**
     * 상품 ID를 포함한 생성자
     * 
     * @param productId 찾을 수 없는 상품 ID
     */
    public ProductNotFoundException(Long productId) {
        super(String.format("상품을 찾을 수 없습니다. (ID: %d)", productId));
    }
    
    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * @param message 예외 메시지
     * @param cause 예외 원인
     */
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}