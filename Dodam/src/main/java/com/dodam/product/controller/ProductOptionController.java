package com.dodam.product.controller;

import com.dodam.common.dto.ApiResponse;
import com.dodam.common.dto.PageResponse;
import com.dodam.product.dto.request.OptionGroupRequest;
import com.dodam.product.dto.request.ProductOptionRequest;
import com.dodam.product.dto.response.ProductOptionResponse;
import com.dodam.product.service.ProductOptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 상품 옵션 관리 REST API 컨트롤러
 * 
 * <p>상품 옵션의 생성, 조회, 수정, 삭제 및 옵션 그룹 관리 기능을 제공하는 RESTful API입니다.</p>
 * 
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/product-options")
@Validated
@Tag(name = "상품 옵션 관리", description = "상품 옵션 CRUD, 옵션 그룹 관리 및 재고 관리 API")
public class ProductOptionController {
    
    private final ProductOptionService productOptionService;
    
    public ProductOptionController(ProductOptionService productOptionService) {
        this.productOptionService = productOptionService;
    }
    
    /**
     * 새로운 상품 옵션을 생성합니다.
     * 
     * @param request 상품 옵션 생성 요청
     * @return 생성된 상품 옵션 정보
     */
    @PostMapping
    @Operation(summary = "상품 옵션 생성", description = "새로운 상품 옵션을 생성합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "옵션 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProductOptionResponse>> createOption(
            @Parameter(description = "상품 옵션 생성 정보", required = true) @Valid @RequestBody ProductOptionRequest request) {
        
        ProductOptionResponse option = productOptionService.createOption(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("상품 옵션이 성공적으로 생성되었습니다.", option));
    }
    
    /**
     * 상품 옵션 정보를 조회합니다.
     * 
     * @param optionId 상품 옵션 ID
     * @return 상품 옵션 정보
     */
    @GetMapping("/{optionId}")
    @Operation(summary = "상품 옵션 조회", description = "옵션 ID로 상품 옵션 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "옵션을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProductOptionResponse>> getOption(
            @Parameter(description = "상품 옵션 ID", required = true) @PathVariable @Min(1) Long optionId) {
        
        ProductOptionResponse option = productOptionService.getOption(optionId);
        
        return ResponseEntity.ok(ApiResponse.success("상품 옵션 정보를 성공적으로 조회했습니다.", option));
    }
    
    /**
     * 상품 옵션을 수정합니다.
     * 
     * @param optionId 상품 옵션 ID
     * @param request 상품 옵션 수정 요청
     * @return 수정된 상품 옵션 정보
     */
    @PutMapping("/{optionId}")
    @Operation(summary = "상품 옵션 수정", description = "상품 옵션 정보를 수정합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "옵션을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProductOptionResponse>> updateOption(
            @Parameter(description = "상품 옵션 ID", required = true) @PathVariable @Min(1) Long optionId,
            @Parameter(description = "상품 옵션 수정 정보", required = true) @Valid @RequestBody ProductOptionRequest request) {
        
        ProductOptionResponse option = productOptionService.updateOption(optionId, request);
        
        return ResponseEntity.ok(ApiResponse.success("상품 옵션이 성공적으로 수정되었습니다.", option));
    }
    
    /**
     * 상품 옵션을 삭제합니다.
     * 
     * @param optionId 상품 옵션 ID
     * @return 삭제 확인 메시지
     */
    @DeleteMapping("/{optionId}")
    @Operation(summary = "상품 옵션 삭제", description = "상품 옵션을 삭제합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "옵션을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> deleteOption(
            @Parameter(description = "상품 옵션 ID", required = true) @PathVariable @Min(1) Long optionId) {
        
        productOptionService.deleteOption(optionId);
        
        return ResponseEntity.ok(ApiResponse.success("상품 옵션이 성공적으로 삭제되었습니다."));
    }
    
    /**
     * 전체 상품 옵션 목록을 페이징하여 조회합니다.
     * 
     * @param pageable 페이징 정보
     * @return 상품 옵션 목록
     */
    @GetMapping
    @Operation(summary = "전체 옵션 목록 조회", description = "전체 상품 옵션을 페이징하여 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<ApiResponse<PageResponse<ProductOptionResponse>>> getAllOptions(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ProductOptionResponse> options = productOptionService.getAllOptions(pageable);
        PageResponse<ProductOptionResponse> pageResponse = PageResponse.of(options);
        
        return ResponseEntity.ok(ApiResponse.success("상품 옵션 목록을 성공적으로 조회했습니다.", pageResponse));
    }
    
    /**
     * 특정 상품의 옵션 목록을 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 해당 상품의 옵션 목록
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "상품별 옵션 조회", description = "특정 상품의 모든 옵션을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<ProductOptionResponse>>> getOptionsByProductId(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId) {
        
        List<ProductOptionResponse> options = productOptionService.getOptionsByProductId(productId);
        
        return ResponseEntity.ok(ApiResponse.success("상품 옵션 목록을 성공적으로 조회했습니다.", options));
    }
    
    /**
     * 특정 상품의 옵션 그룹별 옵션 목록을 조회합니다.
     * 
     * @param productId 상품 ID
     * @param optionGroup 옵션 그룹명
     * @return 해당 그룹의 옵션 목록
     */
    @GetMapping("/product/{productId}/group/{optionGroup}")
    @Operation(summary = "옵션 그룹별 조회", description = "특정 상품의 옵션을 그룹별로 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<ProductOptionResponse>>> getOptionsByGroup(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId,
            @Parameter(description = "옵션 그룹명", required = true) @PathVariable @NotBlank String optionGroup) {
        
        List<ProductOptionResponse> options = productOptionService.getOptionsByGroup(productId, optionGroup);
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("'%s' 그룹의 옵션 목록을 성공적으로 조회했습니다.", optionGroup), options));
    }
    
    /**
     * 특정 상품의 구매 가능한 옵션만 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 구매 가능한 옵션 목록
     */
    @GetMapping("/product/{productId}/available")
    @Operation(summary = "구매 가능한 옵션 조회", description = "재고가 있고 활성화된 옵션만 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<ProductOptionResponse>>> getAvailableOptions(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId) {
        
        List<ProductOptionResponse> options = productOptionService.getAvailableOptions(productId);
        
        return ResponseEntity.ok(ApiResponse.success("구매 가능한 옵션 목록을 성공적으로 조회했습니다.", options));
    }
    
    /**
     * 특정 상품의 옵션 그룹 목록을 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 옵션 그룹 목록
     */
    @GetMapping("/product/{productId}/groups")
    @Operation(summary = "옵션 그룹 목록 조회", description = "특정 상품의 모든 옵션 그룹을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<String>>> getOptionGroups(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId) {
        
        List<String> groups = productOptionService.getOptionGroups(productId);
        
        return ResponseEntity.ok(ApiResponse.success("옵션 그룹 목록을 성공적으로 조회했습니다.", groups));
    }
    
    /**
     * 옵션 그룹을 생성합니다.
     * 
     * @param request 옵션 그룹 생성 요청
     * @return 생성 확인 메시지
     */
    @PostMapping("/groups")
    @Operation(summary = "옵션 그룹 생성", description = "새로운 옵션 그룹을 생성합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "그룹 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> createOptionGroup(
            @Parameter(description = "옵션 그룹 생성 정보", required = true) @Valid @RequestBody OptionGroupRequest request) {
        
        productOptionService.createOptionGroup(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("옵션 그룹이 성공적으로 생성되었습니다."));
    }
    
    /**
     * 옵션 재고를 업데이트합니다.
     * 
     * @param optionId 옵션 ID
     * @param quantity 변경할 재고량 (음수 가능)
     * @return 업데이트된 옵션 정보
     */
    @PatchMapping("/{optionId}/stock")
    @Operation(summary = "옵션 재고 업데이트", description = "특정 옵션의 재고를 변경합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재고 업데이트 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "옵션을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProductOptionResponse>> updateStock(
            @Parameter(description = "옵션 ID", required = true) @PathVariable @Min(1) Long optionId,
            @Parameter(description = "변경할 재고량", required = true) @RequestParam Integer quantity) {
        
        ProductOptionResponse option = productOptionService.updateStock(optionId, quantity);
        
        return ResponseEntity.ok(ApiResponse.success("옵션 재고가 성공적으로 업데이트되었습니다.", option));
    }
    
    /**
     * 옵션 재고를 차감합니다.
     * 
     * @param optionId 옵션 ID
     * @param quantity 차감할 수량
     * @return 차감 성공 여부
     */
    @PostMapping("/{optionId}/deduct-stock")
    @Operation(summary = "옵션 재고 차감", description = "주문이나 예약 시 옵션 재고를 차감합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재고 차감 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "재고 부족 또는 잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "옵션을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Boolean>> deductStock(
            @Parameter(description = "옵션 ID", required = true) @PathVariable @Min(1) Long optionId,
            @Parameter(description = "차감할 수량", required = true) @RequestParam @Min(1) Integer quantity) {
        
        boolean success = productOptionService.deductStock(optionId, quantity);
        
        String message = success 
            ? "옵션 재고가 성공적으로 차감되었습니다." 
            : "재고가 부족하여 차감에 실패했습니다.";
        
        HttpStatus status = success ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        return ResponseEntity.status(status)
            .body(ApiResponse.success(message, success));
    }
    
    /**
     * 옵션 재고를 복원합니다.
     * 
     * @param optionId 옵션 ID
     * @param quantity 복원할 수량
     * @return 복원 성공 여부
     */
    @PostMapping("/{optionId}/restore-stock")
    @Operation(summary = "옵션 재고 복원", description = "취소나 반품 시 옵션 재고를 복원합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재고 복원 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "옵션을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Boolean>> restoreStock(
            @Parameter(description = "옵션 ID", required = true) @PathVariable @Min(1) Long optionId,
            @Parameter(description = "복원할 수량", required = true) @RequestParam @Min(1) Integer quantity) {
        
        boolean success = productOptionService.restoreStock(optionId, quantity);
        
        String message = success 
            ? "옵션 재고가 성공적으로 복원되었습니다." 
            : "재고 복원에 실패했습니다.";
        
        return ResponseEntity.ok(ApiResponse.success(message, success));
    }
    
    /**
     * 옵션 조합의 총 가격을 계산합니다.
     * 
     * @param productId 상품 ID
     * @param optionIds 선택된 옵션 ID 목록
     * @return 계산된 총 가격
     */
    @GetMapping("/product/{productId}/calculate-price")
    @Operation(summary = "옵션 조합 가격 계산", description = "선택된 옵션들의 총 가격을 계산합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "가격 계산 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 옵션 조합", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<BigDecimal>> calculateTotalPrice(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId,
            @Parameter(description = "선택된 옵션 ID 목록", required = true) @RequestParam @NotEmpty List<Long> optionIds) {
        
        BigDecimal totalPrice = productOptionService.calculateTotalPrice(productId, optionIds);
        
        return ResponseEntity.ok(ApiResponse.success("옵션 조합 가격이 성공적으로 계산되었습니다.", totalPrice));
    }
    
    /**
     * 옵션 조합의 유효성을 검증합니다.
     * 
     * @param productId 상품 ID
     * @param optionIds 선택된 옵션 ID 목록
     * @return 유효한 조합인지 여부
     */
    @GetMapping("/product/{productId}/validate-combination")
    @Operation(summary = "옵션 조합 유효성 검증", description = "선택된 옵션 조합이 유효한지 검증합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검증 완료"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Boolean>> validateOptionCombination(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId,
            @Parameter(description = "선택된 옵션 ID 목록", required = true) @RequestParam @NotEmpty List<Long> optionIds) {
        
        boolean isValid = productOptionService.validateOptionCombination(productId, optionIds);
        
        String message = isValid 
            ? "유효한 옵션 조합입니다." 
            : "유효하지 않은 옵션 조합입니다.";
        
        return ResponseEntity.ok(ApiResponse.success(message, isValid));
    }
    
    /**
     * 상품의 옵션별 재고와 메인 상품 재고를 동기화합니다.
     * 
     * @param productId 상품 ID
     * @return 동기화 확인 메시지
     */
    @PostMapping("/product/{productId}/synchronize-stock")
    @Operation(summary = "재고 동기화", description = "옵션별 재고와 메인 상품 재고를 동기화합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "동기화 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> synchronizeStock(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId) {
        
        productOptionService.synchronizeStock(productId);
        
        return ResponseEntity.ok(ApiResponse.success("상품 재고가 성공적으로 동기화되었습니다."));
    }
}