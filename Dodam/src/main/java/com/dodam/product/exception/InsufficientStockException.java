package com.dodam.product.exception;

/**
 * 재고 부족 시 발생하는 예외
 * 상품 구매나 재고 차감 시 재고가 부족한 경우 사용합니다.
 */
public class InsufficientStockException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "INSUFFICIENT_STOCK";

    /**
     * 기본 생성자
     * @param message 예외 메시지
     */
    public InsufficientStockException(String message) {
        super(message);
    }

    /**
     * 에러 코드와 메시지를 받는 생성자
     * @param errorCode 에러 코드
     * @param message 예외 메시지
     */
    public InsufficientStockException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 상품 정보와 재고 상황으로 메시지를 생성하는 편의 생성자
     * @param productName 상품명
     * @param requestedQuantity 요청 수량
     * @param availableStock 현재 재고
     */
    public InsufficientStockException(String productName, int requestedQuantity, int availableStock) {
        super(String.format("상품 '%s'의 재고가 부족합니다. 요청 수량: %d, 현재 재고: %d", 
                          productName, requestedQuantity, availableStock));
    }

    /**
     * 상품 ID와 재고 상황으로 메시지를 생성하는 편의 생성자
     * @param productId 상품 ID
     * @param requestedQuantity 요청 수량
     * @param availableStock 현재 재고
     */
    public InsufficientStockException(Long productId, int requestedQuantity, int availableStock) {
        super(String.format("상품(ID: %d)의 재고가 부족합니다. 요청 수량: %d, 현재 재고: %d", 
                          productId, requestedQuantity, availableStock));
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }
}