package com.dodam.product.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 재고 변경 요청 DTO
 * 
 * 재고 증가/감소, 예약/예약취소 등의 요청에 사용됩니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRequest {
    
    /**
     * 상품 ID
     */
    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;
    
    /**
     * 변경할 재고 수량
     */
    @NotNull(message = "재고 수량은 필수입니다")
    @Positive(message = "재고 수량은 양수여야 합니다")
    private Integer quantity;
    
    /**
     * 재고 변경 사유
     */
    private String reason;
    
    /**
     * 요청자 ID (관리자, 시스템 등)
     */
    private String requestedBy;
    
    /**
     * 참조 번호 (주문번호, 입고번호 등)
     */
    private String referenceNumber;
    
    /**
     * 창고 ID (다중 창고 지원)
     */
    private Long warehouseId;
    
    /**
     * 예약 ID (예약 확정/취소 시 사용)
     */
    private String reservationId;
}