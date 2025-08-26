package com.dodam.product.controller;

import com.dodam.common.dto.ApiResponse;
import com.dodam.common.dto.PageResponse;
import com.dodam.product.dto.response.BrandResponse;
import com.dodam.product.dto.response.ProductResponse;
import com.dodam.product.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 브랜드 관리 REST API 컨트롤러
 * 
 * <p>브랜드 조회 및 브랜드별 상품 조회 기능을 제공하는 RESTful API입니다.</p>
 * 
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/brands")
@Validated
@Tag(name = "브랜드 관리", description = "브랜드 조회 및 관리 API")
public class BrandController {
    
    private final BrandService brandService;
    
    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }
    
    /**
     * 모든 활성화된 브랜드 목록을 조회합니다.
     * 
     * @return 브랜드 목록
     */
    @GetMapping
    @Operation(summary = "브랜드 목록 조회", description = "모든 활성화된 브랜드 목록을 이름 순서에 따라 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getBrands() {
        
        List<BrandResponse> brands = brandService.findAllActiveBrands();
        
        return ResponseEntity.ok(ApiResponse.success("브랜드 목록을 성공적으로 조회했습니다.", brands));
    }
    
    /**
     * 브랜드 상세 정보를 조회합니다.
     * 
     * @param id 브랜드 ID
     * @return 브랜드 상세 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "브랜드 상세 조회", description = "브랜드 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "브랜드를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<BrandResponse>> getBrand(
            @Parameter(description = "브랜드 ID", required = true) @PathVariable @Min(1) Long id) {
        
        BrandResponse brand = brandService.findById(id);
        
        return ResponseEntity.ok(ApiResponse.success("브랜드 정보를 성공적으로 조회했습니다.", brand));
    }
    
    /**
     * 브랜드명으로 브랜드를 검색합니다.
     * 
     * @param name 검색할 브랜드명 (부분 일치)
     * @return 검색된 브랜드 목록
     */
    @GetMapping("/search")
    @Operation(summary = "브랜드 검색", description = "브랜드명으로 브랜드를 검색합니다. (부분 일치)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<BrandResponse>>> searchBrands(
            @Parameter(description = "검색할 브랜드명", required = true) @RequestParam String name) {
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("검색할 브랜드명을 입력해주세요.");
        }
        
        List<BrandResponse> brands = brandService.findByBrandNameContaining(name.trim());
        
        return ResponseEntity.ok(ApiResponse.success("브랜드 검색을 성공적으로 완료했습니다.", brands));
    }
    
    /**
     * 브랜드별 상품을 조회합니다.
     * 
     * @param id 브랜드 ID
     * @param pageable 페이징 정보 (기본: 0페이지, 20개, 생성일 역순)
     * @return 해당 브랜드의 상품 목록
     */
    @GetMapping("/{id}/products")
    @Operation(summary = "브랜드별 상품 조회", description = "특정 브랜드의 상품들을 페이징하여 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "브랜드를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByBrand(
            @Parameter(description = "브랜드 ID", required = true) @PathVariable @Min(1) Long id,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ProductResponse> products = brandService.findProductsByBrand(id, pageable);
        PageResponse<ProductResponse> pageResponse = PageResponse.of(products);
        
        return ResponseEntity.ok(ApiResponse.success("브랜드별 상품 목록을 성공적으로 조회했습니다.", pageResponse));
    }
    
    /**
     * 브랜드의 활성화 여부를 확인합니다.
     * 
     * @param id 브랜드 ID
     * @return 활성화 여부
     */
    @GetMapping("/{id}/active")
    @Operation(summary = "브랜드 활성화 상태 확인", description = "브랜드가 활성화되어 있는지 확인합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "확인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "브랜드를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Boolean>> isBrandActive(
            @Parameter(description = "브랜드 ID", required = true) @PathVariable @Min(1) Long id) {
        
        // 브랜드 존재 여부 먼저 확인
        if (!brandService.existsById(id)) {
            throw new IllegalArgumentException("브랜드를 찾을 수 없습니다. ID: " + id);
        }
        
        boolean isActive = brandService.isActiveBrand(id);
        String message = isActive ? "활성화된 브랜드입니다." : "비활성화된 브랜드입니다.";
        
        return ResponseEntity.ok(ApiResponse.success(message, isActive));
    }
}