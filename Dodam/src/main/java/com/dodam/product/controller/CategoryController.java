package com.dodam.product.controller;

import com.dodam.common.dto.ApiResponse;
import com.dodam.common.dto.PageResponse;
import com.dodam.product.dto.response.CategoryResponse;
import com.dodam.product.dto.response.ProductResponse;
import com.dodam.product.service.CategoryService;
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
 * 카테고리 관리 REST API 컨트롤러
 * 
 * <p>카테고리 조회 및 카테고리별 상품 조회 기능을 제공하는 RESTful API입니다.</p>
 * 
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/categories")
@Validated
@Tag(name = "카테고리 관리", description = "카테고리 조회 및 관리 API")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    /**
     * 모든 활성화된 카테고리 목록을 조회합니다.
     * 
     * @return 카테고리 목록
     */
    @GetMapping
    @Operation(summary = "카테고리 목록 조회", description = "모든 활성화된 카테고리 목록을 표시 순서에 따라 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        
        List<CategoryResponse> categories = categoryService.findAllActiveCategories();
        
        return ResponseEntity.ok(ApiResponse.success("카테고리 목록을 성공적으로 조회했습니다.", categories));
    }
    
    /**
     * 카테고리 상세 정보를 조회합니다.
     * 
     * @param id 카테고리 ID
     * @return 카테고리 상세 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "카테고리 상세 조회", description = "카테고리 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable @Min(1) Long id) {
        
        CategoryResponse category = categoryService.findById(id);
        
        return ResponseEntity.ok(ApiResponse.success("카테고리 정보를 성공적으로 조회했습니다.", category));
    }
    
    /**
     * 루트 카테고리 목록을 조회합니다. (최상위 카테고리)
     * 
     * @return 루트 카테고리 목록
     */
    @GetMapping("/root")
    @Operation(summary = "루트 카테고리 조회", description = "최상위 카테고리(부모가 없는 카테고리) 목록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {
        
        List<CategoryResponse> rootCategories = categoryService.findRootCategories();
        
        return ResponseEntity.ok(ApiResponse.success("루트 카테고리 목록을 성공적으로 조회했습니다.", rootCategories));
    }
    
    /**
     * 특정 부모 카테고리의 하위 카테고리를 조회합니다.
     * 
     * @param parentId 부모 카테고리 ID
     * @return 하위 카테고리 목록
     */
    @GetMapping("/parent/{parentId}/children")
    @Operation(summary = "하위 카테고리 조회", description = "특정 부모 카테고리의 하위 카테고리 목록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "부모 카테고리를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getSubCategories(
            @Parameter(description = "부모 카테고리 ID", required = true) @PathVariable @Min(1) Long parentId) {
        
        // 부모 카테고리 존재 여부 확인
        if (!categoryService.existsById(parentId)) {
            throw new IllegalArgumentException("부모 카테고리를 찾을 수 없습니다. ID: " + parentId);
        }
        
        List<CategoryResponse> subCategories = categoryService.findByParentCategoryId(parentId);
        
        return ResponseEntity.ok(ApiResponse.success("하위 카테고리 목록을 성공적으로 조회했습니다.", subCategories));
    }
    
    /**
     * 카테고리별 상품을 조회합니다.
     * 
     * @param id 카테고리 ID
     * @param pageable 페이징 정보 (기본: 0페이지, 20개, 생성일 역순)
     * @return 해당 카테고리의 상품 목록
     */
    @GetMapping("/{id}/products")
    @Operation(summary = "카테고리별 상품 조회", description = "특정 카테고리에 속한 상품들을 페이징하여 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByCategory(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable @Min(1) Long id,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ProductResponse> products = categoryService.findProductsByCategory(id, pageable);
        PageResponse<ProductResponse> pageResponse = PageResponse.of(products);
        
        return ResponseEntity.ok(ApiResponse.success("카테고리별 상품 목록을 성공적으로 조회했습니다.", pageResponse));
    }
    
    /**
     * 카테고리의 활성화 여부를 확인합니다.
     * 
     * @param id 카테고리 ID
     * @return 활성화 여부
     */
    @GetMapping("/{id}/active")
    @Operation(summary = "카테고리 활성화 상태 확인", description = "카테고리가 활성화되어 있는지 확인합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "확인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Boolean>> isCategoryActive(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable @Min(1) Long id) {
        
        // 카테고리 존재 여부 먼저 확인
        if (!categoryService.existsById(id)) {
            throw new IllegalArgumentException("카테고리를 찾을 수 없습니다. ID: " + id);
        }
        
        boolean isActive = categoryService.isActiveCategory(id);
        String message = isActive ? "활성화된 카테고리입니다." : "비활성화된 카테고리입니다.";
        
        return ResponseEntity.ok(ApiResponse.success(message, isActive));
    }
}