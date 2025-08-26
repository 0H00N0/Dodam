package com.dodam.product.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 재고 이동 요청 DTO
 * 
 * 창고간 재고 이동에 사용됩니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementRequest {
    
    /**
     * 상품 ID
     */
    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;
    
    /**
     * 출발 창고 ID
     */
    @NotNull(message = "출발 창고 ID는 필수입니다")
    private Long fromWarehouseId;
    
    /**
     * 도착 창고 ID
     */
    @NotNull(message = "도착 창고 ID는 필수입니다")
    private Long toWarehouseId;
    
    /**
     * 이동할 재고 수량
     */
    @NotNull(message = "이동 수량은 필수입니다")
    @Positive(message = "이동 수량은 양수여야 합니다")
    private Integer quantity;
    
    /**
     * 이동 사유
     */
    private String reason;
    
    /**
     * 요청자 ID
     */
    private String requestedBy;
    
    /**
     * 이동 참조 번호 (이동지시서 번호 등)
     */
    private String movementReferenceNumber;
}