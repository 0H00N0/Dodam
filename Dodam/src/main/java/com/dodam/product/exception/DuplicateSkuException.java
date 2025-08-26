package com.dodam.product.exception;

/**
 * SKU가 중복될 때 발생하는 예외
 * 
 * @since 1.0.0
 */
public class DuplicateSkuException extends RuntimeException {
    
    private static final String DEFAULT_MESSAGE = "중복된 SKU입니다.";
    
    /**
     * 기본 생성자
     */
    public DuplicateSkuException() {
        super(DEFAULT_MESSAGE);
    }
    
    /**
     * 메시지를 포함한 생성자
     * 
     * @param message 예외 메시지
     */
    public DuplicateSkuException(String message) {
        super(message);
    }
    
    /**
     * SKU와 상품 정보를 포함한 생성자
     * 
     * @param sku 중복된 SKU
     * @param productName 상품명
     */
    public static DuplicateSkuException withSku(String sku, String productName) {
        return new DuplicateSkuException(
            String.format("이미 존재하는 SKU입니다: %s (상품: %s)", sku, productName)
        );
    }
    
    /**
     * SKU만 포함한 정적 팩토리 메서드
     * 
     * @param sku 중복된 SKU
     */
    public static DuplicateSkuException withSku(String sku) {
        return new DuplicateSkuException(
            String.format("이미 존재하는 SKU입니다: %s", sku)
        );
    }
    
    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * @param message 예외 메시지
     * @param cause 예외 원인
     */
    public DuplicateSkuException(String message, Throwable cause) {
        super(message, cause);
    }
}