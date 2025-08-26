package com.dodam.product.controller;

import com.dodam.common.dto.ApiResponse;
import com.dodam.common.dto.PageResponse;
import com.dodam.product.common.enums.ProductStatus;
import com.dodam.product.dto.request.ProductCreateRequest;
import com.dodam.product.dto.request.ProductUpdateRequest;
import com.dodam.product.dto.response.ProductDetailResponse;
import com.dodam.product.dto.response.ProductResponse;
import com.dodam.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
 * 상품 관리 REST API 컨트롤러
 * 
 * <p>상품의 등록, 조회, 수정, 삭제 등의 기능을 제공하는 RESTful API입니다.</p>
 * 
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/products")
@Validated
@Tag(name = "상품 관리", description = "상품의 CRUD 및 상태 관리 API")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * 상품 목록을 페이징하여 조회합니다.
     * 
     * @param status 상품 상태 필터 (선택사항)
     * @param categoryId 카테고리 ID 필터 (선택사항)
     * @param brandId 브랜드 ID 필터 (선택사항)
     * @param pageable 페이징 정보 (기본: 0페이지, 20개, 생성일 역순)
     * @return 상품 목록
     */
    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "조건에 따라 상품 목록을 페이징하여 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @Parameter(description = "상품 상태") @RequestParam(required = false) ProductStatus status,
            @Parameter(description = "카테고리 ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "브랜드 ID") @RequestParam(required = false) Long brandId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ProductResponse> products;
        
        if (status != null) {
            products = productService.findByStatus(status, pageable);
        } else if (categoryId != null) {
            products = productService.findByCategory(categoryId, pageable);
        } else if (brandId != null) {
            products = productService.findByBrand(brandId, pageable);
        } else {
            products = productService.findAll(pageable);
        }
        
        PageResponse<ProductResponse> pageResponse = PageResponse.of(products);
        
        return ResponseEntity.ok(ApiResponse.success("상품 목록을 성공적으로 조회했습니다.", pageResponse));
    }
    
    /**
     * 상품 상세 정보를 조회합니다.
     * 
     * @param id 상품 ID
     * @return 상품 상세 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long id) {
        
        ProductDetailResponse product = productService.findDetailById(id);
        
        return ResponseEntity.ok(ApiResponse.success("상품 정보를 성공적으로 조회했습니다.", product));
    }
    
    /**
     * 새로운 상품을 등록합니다.
     * 
     * @param request 상품 등록 요청
     * @return 등록된 상품 정보
     */
    @PostMapping
    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복된 SKU", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Parameter(description = "상품 등록 정보", required = true) @Valid @RequestBody ProductCreateRequest request) {
        
        ProductResponse product = productService.createProduct(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("상품이 성공적으로 등록되었습니다.", product));
    }
    
    /**
     * 상품 정보를 수정합니다.
     * 
     * @param id 상품 ID
     * @param request 상품 수정 요청
     * @return 수정된 상품 정보
     */
    @PutMapping("/{id}")
    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long id,
            @Parameter(description = "상품 수정 정보", required = true) @Valid @RequestBody ProductUpdateRequest request) {
        
        ProductResponse product = productService.updateProduct(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("상품이 성공적으로 수정되었습니다.", product));
    }
    
    /**
     * 상품을 삭제합니다. (소프트 삭제)
     * 
     * @param id 상품 ID
     * @return 삭제 확인 메시지
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "상품 삭제", description = "상품을 소프트 삭제합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long id) {
        
        productService.deleteProduct(id);
        
        return ResponseEntity.ok(ApiResponse.success("상품이 성공적으로 삭제되었습니다."));
    }
    
    /**
     * 상품 상태를 변경합니다.
     * 
     * @param id 상품 ID
     * @param status 변경할 상태
     * @return 상태가 변경된 상품 정보
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "상품 상태 변경", description = "상품의 상태를 변경합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 허용되지 않는 상태 전이", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStatus(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long id,
            @Parameter(description = "변경할 상태", required = true) @RequestParam @NotNull ProductStatus status) {
        
        ProductResponse product = productService.updateProductStatus(id, status);
        
        return ResponseEntity.ok(ApiResponse.success("상품 상태가 성공적으로 변경되었습니다.", product));
    }
    
    /**
     * 상품을 검색합니다.
     * 
     * @param keyword 검색 키워드 (상품명, 브랜드명, 카테고리명)
     * @param status 상품 상태 필터 (선택사항)
     * @param categoryId 카테고리 ID 필터 (선택사항)
     * @param brandId 브랜드 ID 필터 (선택사항)
     * @param minPrice 최소 가격 (선택사항)
     * @param maxPrice 최대 가격 (선택사항)
     * @param pageable 페이징 정보
     * @return 검색된 상품 목록
     */
    @GetMapping("/search")
    @Operation(summary = "상품 검색", description = "키워드와 조건으로 상품을 검색합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "상품 상태 필터") @RequestParam(required = false) ProductStatus status,
            @Parameter(description = "카테고리 ID 필터") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "브랜드 ID 필터") @RequestParam(required = false) Long brandId,
            @Parameter(description = "최소 가격") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "최대 가격") @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ProductResponse> products;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.searchByKeyword(keyword.trim(), status, pageable);
        } else {
            products = productService.findBySearchCriteria(categoryId, brandId, minPrice, maxPrice, status, pageable);
        }
        
        PageResponse<ProductResponse> pageResponse = PageResponse.of(products);
        
        return ResponseEntity.ok(ApiResponse.success("상품 검색을 성공적으로 완료했습니다.", pageResponse));
    }
    
    /**
     * 상품의 총 가격을 계산합니다. (옵션 포함)
     * 
     * @param id 상품 ID
     * @param selectedOptionIds 선택된 옵션 ID 목록
     * @return 계산된 총 가격
     */
    @GetMapping("/{id}/price")
    @Operation(summary = "상품 가격 계산", description = "선택된 옵션을 포함한 상품의 총 가격을 계산합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계산 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<BigDecimal>> calculateTotalPrice(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long id,
            @Parameter(description = "선택된 옵션 ID 목록") @RequestParam(required = false, defaultValue = "") List<Long> selectedOptionIds) {
        
        BigDecimal totalPrice = productService.calculateTotalPrice(id, selectedOptionIds);
        
        return ResponseEntity.ok(ApiResponse.success("상품 가격이 성공적으로 계산되었습니다.", totalPrice));
    }
    
    /**
     * 상품의 주문 가능 여부를 확인합니다.
     * 
     * @param id 상품 ID
     * @param quantity 주문하려는 수량 (기본값: 1)
     * @return 주문 가능 여부
     */
    @GetMapping("/{id}/orderable")
    @Operation(summary = "주문 가능 여부 확인", description = "상품의 주문 가능 여부를 확인합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "확인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Boolean>> checkOrderable(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long id,
            @Parameter(description = "주문 수량") @RequestParam(defaultValue = "1") @Min(1) int quantity) {
        
        boolean orderable = productService.isOrderable(id, quantity);
        
        String message = orderable ? "주문 가능한 상품입니다." : "주문할 수 없는 상품입니다.";
        
        return ResponseEntity.ok(ApiResponse.success(message, orderable));
    }
    
    /**
     * 인기 상품 목록을 조회합니다.
     * 
     * @param limit 조회할 개수 (기본값: 10, 최대: 50)
     * @return 인기 상품 목록
     */
    @GetMapping("/popular")
    @Operation(summary = "인기 상품 조회", description = "인기 상품 목록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getPopularProducts(
            @Parameter(description = "조회할 개수") @RequestParam(defaultValue = "10") @Min(1) int limit) {
        
        // 최대 50개로 제한
        int actualLimit = Math.min(limit, 50);
        List<ProductResponse> popularProducts = productService.findPopularProducts(actualLimit);
        
        return ResponseEntity.ok(ApiResponse.success("인기 상품 목록을 성공적으로 조회했습니다.", popularProducts));
    }
}