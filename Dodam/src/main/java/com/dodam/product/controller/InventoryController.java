package com.dodam.product.controller;

import com.dodam.common.dto.ApiResponse;
import com.dodam.product.dto.request.InventoryRequest;
import com.dodam.product.dto.request.StockMovementRequest;
import com.dodam.product.dto.response.InventoryResponse;
import com.dodam.product.entity.StockMovementHistory;
import com.dodam.product.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 재고 관리 REST API 컨트롤러
 * 
 * <p>재고 조회, 조정, 예약/취소, 이력 관리 등의 기능을 제공하는 RESTful API입니다.</p>
 * 
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/inventory")
@Validated
@Tag(name = "재고 관리", description = "재고 조회, 조정, 예약 및 이력 관리 API")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    /**
     * 상품의 현재 재고 정보를 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 재고 정보
     */
    @GetMapping("/{productId}")
    @Operation(summary = "재고 조회", description = "상품 ID로 현재 재고 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "재고 정보를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId) {
        
        Optional<InventoryResponse> inventory = inventoryService.getInventory(productId);
        
        if (inventory.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("해당 상품의 재고 정보를 찾을 수 없습니다.", (InventoryResponse)null));
        }
        
        return ResponseEntity.ok(ApiResponse.success("재고 정보를 성공적으로 조회했습니다.", inventory.get()));
    }
    
    /**
     * 여러 상품의 재고 정보를 일괄 조회합니다.
     * 
     * @param productIds 상품 ID 목록
     * @return 재고 정보 목록
     */
    @GetMapping("/bulk")
    @Operation(summary = "재고 일괄 조회", description = "여러 상품의 재고 정보를 한번에 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getBulkInventory(
            @Parameter(description = "상품 ID 목록", required = true) @RequestParam @NotEmpty List<Long> productIds) {
        
        List<InventoryResponse> inventories = inventoryService.getBulkInventory(productIds);
        
        return ResponseEntity.ok(ApiResponse.success("재고 정보를 성공적으로 일괄 조회했습니다.", inventories));
    }
    
    /**
     * 재고를 감소시킵니다. (판매, 사용 시)
     * 
     * @param request 재고 감소 요청
     * @return 업데이트된 재고 정보
     */
    @PostMapping("/decrease")
    @Operation(summary = "재고 감소", description = "재고를 감소시킵니다. 주문 확정이나 상품 사용 시 호출합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재고 감소 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 재고 부족", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<InventoryResponse>> decreaseStock(
            @Parameter(description = "재고 감소 요청", required = true) @Valid @RequestBody InventoryRequest request) {
        
        InventoryResponse inventory = inventoryService.decreaseStock(request);
        
        return ResponseEntity.ok(ApiResponse.success("재고가 성공적으로 감소되었습니다.", inventory));
    }
    
    /**
     * 재고를 증가시킵니다. (입고, 반품 시)
     * 
     * @param request 재고 증가 요청
     * @return 업데이트된 재고 정보
     */
    @PostMapping("/increase")
    @Operation(summary = "재고 증가", description = "재고를 증가시킵니다. 입고나 반품 시 호출합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재고 증가 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<InventoryResponse>> increaseStock(
            @Parameter(description = "재고 증가 요청", required = true) @Valid @RequestBody InventoryRequest request) {
        
        InventoryResponse inventory = inventoryService.increaseStock(request);
        
        return ResponseEntity.ok(ApiResponse.success("재고가 성공적으로 증가되었습니다.", inventory));
    }
    
    /**
     * 재고를 예약합니다. (장바구니, 주문 대기 시)
     * 
     * @param request 재고 예약 요청
     * @return 업데이트된 재고 정보
     */
    @PostMapping("/reserve")
    @Operation(summary = "재고 예약", description = "재고를 예약합니다. 장바구니 담기나 주문 대기 시 호출합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재고 예약 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 재고 부족", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<InventoryResponse>> reserveStock(
            @Parameter(description = "재고 예약 요청", required = true) @Valid @RequestBody InventoryRequest request) {
        
        InventoryResponse inventory = inventoryService.reserveStock(request);
        
        return ResponseEntity.ok(ApiResponse.success("재고가 성공적으로 예약되었습니다.", inventory));
    }
    
    /**
     * 예약된 재고를 확정합니다. (주문 완료 시)
     * 
     * @param request 예약 확정 요청
     * @return 업데이트된 재고 정보
     */
    @PostMapping("/confirm-reservation")
    @Operation(summary = "재고 예약 확정", description = "예약된 재고를 확정합니다. 주문이 완료되었을 때 호출합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예약 확정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<InventoryResponse>> confirmReservation(
            @Parameter(description = "예약 확정 요청", required = true) @Valid @RequestBody InventoryRequest request) {
        
        InventoryResponse inventory = inventoryService.confirmReservation(request);
        
        return ResponseEntity.ok(ApiResponse.success("재고 예약이 성공적으로 확정되었습니다.", inventory));
    }
    
    /**
     * 예약된 재고를 취소합니다. (주문 취소, 장바구니 제거 시)
     * 
     * @param request 예약 취소 요청
     * @return 업데이트된 재고 정보
     */
    @PostMapping("/cancel-reservation")
    @Operation(summary = "재고 예약 취소", description = "예약된 재고를 취소합니다. 주문 취소나 장바구니에서 제거할 때 호출합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예약 취소 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<InventoryResponse>> cancelReservation(
            @Parameter(description = "예약 취소 요청", required = true) @Valid @RequestBody InventoryRequest request) {
        
        InventoryResponse inventory = inventoryService.cancelReservation(request);
        
        return ResponseEntity.ok(ApiResponse.success("재고 예약이 성공적으로 취소되었습니다.", inventory));
    }
    
    /**
     * 재고를 이동합니다. (창고간 이동 시)
     * 
     * @param request 재고 이동 요청
     * @return 업데이트된 재고 정보
     */
    @PostMapping("/move")
    @Operation(summary = "재고 이동", description = "재고를 다른 창고로 이동합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재고 이동 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 재고 부족", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 또는 창고를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<InventoryResponse>> moveStock(
            @Parameter(description = "재고 이동 요청", required = true) @Valid @RequestBody StockMovementRequest request) {
        
        InventoryResponse inventory = inventoryService.moveStock(request);
        
        return ResponseEntity.ok(ApiResponse.success("재고가 성공적으로 이동되었습니다.", inventory));
    }
    
    /**
     * 재고 변경 이력을 조회합니다.
     * 
     * @param productId 상품 ID
     * @param limit 조회할 이력 개수 (기본값: 50, 최대: 500)
     * @return 재고 변경 이력 목록
     */
    @GetMapping("/{productId}/history")
    @Operation(summary = "재고 이력 조회", description = "상품의 재고 변경 이력을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<StockMovementHistory>>> getStockHistory(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId,
            @Parameter(description = "조회할 이력 개수") @RequestParam(defaultValue = "50") @Min(1) int limit) {
        
        // 최대 500개로 제한
        int actualLimit = Math.min(limit, 500);
        List<StockMovementHistory> history = inventoryService.getStockHistory(productId, actualLimit);
        
        return ResponseEntity.ok(ApiResponse.success("재고 이력을 성공적으로 조회했습니다.", history));
    }
    
    /**
     * 재고 부족 상품 목록을 조회합니다.
     * 
     * @param threshold 기준 재고량 (이하 시 알림 대상, 기본값: 10)
     * @return 재고 부족 상품 목록
     */
    @GetMapping("/low-stock")
    @Operation(summary = "재고 부족 상품 조회", description = "재고가 부족한 상품 목록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockProducts(
            @Parameter(description = "기준 재고량") @RequestParam(defaultValue = "10") @Min(0) int threshold) {
        
        List<InventoryResponse> lowStockProducts = inventoryService.getLowStockProducts(threshold);
        
        String message = lowStockProducts.isEmpty() 
            ? "재고 부족 상품이 없습니다." 
            : String.format("재고 부족 상품 %d개를 조회했습니다.", lowStockProducts.size());
        
        return ResponseEntity.ok(ApiResponse.success(message, lowStockProducts));
    }
    
    /**
     * 재고 일관성을 검증합니다.
     * 
     * @param productId 상품 ID
     * @return 검증 결과
     */
    @GetMapping("/{productId}/validate")
    @Operation(summary = "재고 일관성 검증", description = "상품의 재고 데이터 일관성을 검증합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검증 완료"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Boolean>> validateStockConsistency(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId) {
        
        boolean isConsistent = inventoryService.validateStockConsistency(productId);
        
        String message = isConsistent 
            ? "재고 데이터가 일관성을 유지하고 있습니다." 
            : "재고 데이터에 불일치가 발견되었습니다. 관리자에게 문의하세요.";
        
        HttpStatus status = isConsistent ? HttpStatus.OK : HttpStatus.CONFLICT;
        
        return ResponseEntity.status(status)
            .body(ApiResponse.success(message, isConsistent));
    }
}