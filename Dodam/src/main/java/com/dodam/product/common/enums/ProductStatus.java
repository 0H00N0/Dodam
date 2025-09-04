package com.dodam.product.common.enums;

/**
 * 상품 상태 열거형
 * 
 * <p>상품의 라이프사이클을 나타내는 상태값입니다.</p>
 * 
 * @since 1.0.0
 */
public enum ProductStatus {
    
    /**
     * 작성중 - 상품 정보 입력 단계
     */
    DRAFT("작성중"),
    
    /**
     * 판매중 - 고객에게 노출되어 주문 가능한 상태
     */
    ACTIVE("판매중"),
    
    /**
     * 판매중지 - 일시적으로 판매를 중단한 상태
     */
    INACTIVE("판매중지"),
    
    /**
     * 삭제됨 - 논리적으로 삭제된 상태 (복구 가능)
     */
    DELETED("삭제됨");
    
    private final String description;
    
    ProductStatus(String description) {
        this.description = description;
    }
    
    /**
     * 상태에 대한 한국어 설명을 반환합니다.
     * 
     * @return 상태 설명
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 상태 전이가 가능한지 검증합니다.
     * 
     * @param newStatus 변경하려는 새로운 상태
     * @return 전이 가능 여부
     */
    public boolean canTransitionTo(ProductStatus newStatus) {
        return switch (this) {
            case DRAFT -> newStatus == ACTIVE || newStatus == DELETED;
            case ACTIVE -> newStatus == INACTIVE || newStatus == DELETED;
            case INACTIVE -> newStatus == ACTIVE || newStatus == DELETED;
            case DELETED -> false; // 삭제된 상품은 다른 상태로 전이 불가
        };
    }
    
    /**
     * 주문 가능한 상태인지 확인합니다.
     * 
     * @return 주문 가능 여부
     */
    public boolean isOrderable() {
        return this == ACTIVE;
    }
}