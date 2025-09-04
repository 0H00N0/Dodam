package com.dodam.product.controller;

import com.dodam.common.dto.ApiResponse;
import com.dodam.product.dto.request.ImageUploadRequest;
import com.dodam.product.dto.response.ImageMetadata;
import com.dodam.product.dto.response.ProductImageResponse;
import com.dodam.product.service.ProductImageService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 상품 이미지 관리 REST API 컨트롤러
 * 
 * <p>상품 이미지의 업로드, 삭제, 순서 관리, 썸네일 생성 및 대표 이미지 설정 기능을 제공하는 RESTful API입니다.</p>
 * 
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/product-images")
@Validated
@Tag(name = "상품 이미지 관리", description = "상품 이미지 업로드, 삭제, 썸네일 생성 및 대표 이미지 설정 API")
public class ProductImageController {
    
    private final ProductImageService productImageService;
    
    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }
    
    /**
     * 단일 이미지를 업로드합니다.
     * 
     * @param productId 상품 ID
     * @param file 업로드할 이미지 파일
     * @param imageType 이미지 타입 (기본값: DETAIL)
     * @param orderIndex 이미지 순서 (기본값: 1)
     * @param description 이미지 설명
     * @param title 이미지 제목
     * @param isMain 대표 이미지 여부 (기본값: false)
     * @param generateThumbnail 썸네일 생성 여부 (기본값: true)
     * @return 업로드된 이미지 정보
     */
    @PostMapping(value = "/product/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드", description = "상품에 단일 이미지를 업로드합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "업로드 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파일 또는 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "파일 크기 초과", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProductImageResponse>> uploadImage(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId,
            @Parameter(description = "업로드할 이미지 파일", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "이미지 타입") @RequestParam(defaultValue = "DETAIL") String imageType,
            @Parameter(description = "이미지 순서") @RequestParam(defaultValue = "1") @Min(1) Integer orderIndex,
            @Parameter(description = "이미지 설명") @RequestParam(required = false) String description,
            @Parameter(description = "이미지 제목") @RequestParam(required = false) String title,
            @Parameter(description = "대표 이미지 여부") @RequestParam(defaultValue = "false") Boolean isMain,
            @Parameter(description = "썸네일 생성 여부") @RequestParam(defaultValue = "true") Boolean generateThumbnail) {
        
        // ImageUploadRequest 객체 생성
        ImageUploadRequest request = ImageUploadRequest.builder()
            .imageType(imageType)
            .orderIndex(orderIndex)
            .description(description)
            .title(title)
            .isMain(isMain)
            .generateThumbnail(generateThumbnail)
            .build();
        
        ProductImageResponse image = productImageService.uploadImage(productId, file, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("이미지가 성공적으로 업로드되었습니다.", image));
    }
    
    /**
     * 다중 이미지를 업로드합니다.
     * 
     * @param productId 상품 ID
     * @param files 업로드할 이미지 파일들
     * @return 업로드된 이미지 정보들
     */
    @PostMapping(value = "/product/{productId}/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "다중 이미지 업로드", description = "상품에 여러 이미지를 한번에 업로드합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "업로드 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파일 또는 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> uploadMultipleImages(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId,
            @Parameter(description = "업로드할 이미지 파일들", required = true) @RequestParam("files") List<MultipartFile> files) {
        
        // 각 파일에 대한 기본 요청 정보 생성
        List<ImageUploadRequest> requests = files.stream()
            .map(file -> ImageUploadRequest.builder()
                .imageType("DETAIL")
                .orderIndex(1)
                .generateThumbnail(true)
                .build())
            .toList();
        
        List<ProductImageResponse> images = productImageService.uploadMultipleImages(productId, files, requests);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                String.format("%d개의 이미지가 성공적으로 업로드되었습니다.", images.size()), images));
    }
    
    /**
     * 이미지를 삭제합니다.
     * 
     * @param imageId 삭제할 이미지 ID
     * @return 삭제 확인 메시지
     */
    @DeleteMapping("/{imageId}")
    @Operation(summary = "이미지 삭제", description = "특정 이미지를 삭제합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @Parameter(description = "이미지 ID", required = true) @PathVariable @Min(1) Long imageId) {
        
        productImageService.deleteImage(imageId);
        
        return ResponseEntity.ok(ApiResponse.success("이미지가 성공적으로 삭제되었습니다."));
    }
    
    /**
     * 상품의 모든 이미지를 삭제합니다.
     * 
     * @param productId 상품 ID
     * @return 삭제 확인 메시지
     */
    @DeleteMapping("/product/{productId}")
    @Operation(summary = "상품 이미지 전체 삭제", description = "특정 상품의 모든 이미지를 삭제합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> deleteAllProductImages(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId) {
        
        productImageService.deleteAllProductImages(productId);
        
        return ResponseEntity.ok(ApiResponse.success("상품의 모든 이미지가 성공적으로 삭제되었습니다."));
    }
    
    /**
     * 상품의 이미지 목록을 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 상품의 이미지 목록
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "상품 이미지 목록 조회", description = "특정 상품의 모든 이미지를 순서대로 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getProductImages(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId) {
        
        List<ProductImageResponse> images = productImageService.getProductImages(productId);
        
        return ResponseEntity.ok(ApiResponse.success("상품 이미지 목록을 성공적으로 조회했습니다.", images));
    }
    
    /**
     * 특정 타입의 이미지 목록을 조회합니다.
     * 
     * @param productId 상품 ID
     * @param imageType 이미지 타입
     * @return 해당 타입의 이미지 목록
     */
    @GetMapping("/product/{productId}/type/{imageType}")
    @Operation(summary = "타입별 이미지 조회", description = "특정 상품의 특정 타입 이미지들을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getImagesByType(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId,
            @Parameter(description = "이미지 타입", required = true) @PathVariable @NotBlank String imageType) {
        
        List<ProductImageResponse> images = productImageService.getImagesByType(productId, imageType);
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("'%s' 타입의 이미지 목록을 성공적으로 조회했습니다.", imageType), images));
    }
    
    /**
     * 대표 이미지를 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 대표 이미지 정보
     */
    @GetMapping("/product/{productId}/main")
    @Operation(summary = "대표 이미지 조회", description = "상품의 대표 이미지를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 또는 대표 이미지를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProductImageResponse>> getMainImage(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId) {
        
        ProductImageResponse mainImage = productImageService.getMainImage(productId);
        
        if (mainImage == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("해당 상품의 대표 이미지를 찾을 수 없습니다.", (ProductImageResponse)null));
        }
        
        return ResponseEntity.ok(ApiResponse.success("대표 이미지를 성공적으로 조회했습니다.", mainImage));
    }
    
    /**
     * 대표 이미지로 설정합니다.
     * 
     * @param productId 상품 ID
     * @param imageId 대표 이미지로 설정할 이미지 ID
     * @return 설정 확인 메시지
     */
    @PatchMapping("/product/{productId}/main/{imageId}")
    @Operation(summary = "대표 이미지 설정", description = "특정 이미지를 상품의 대표 이미지로 설정합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 또는 이미지를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> setMainImage(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId,
            @Parameter(description = "이미지 ID", required = true) @PathVariable @Min(1) Long imageId) {
        
        productImageService.setMainImage(productId, imageId);
        
        return ResponseEntity.ok(ApiResponse.success("대표 이미지가 성공적으로 설정되었습니다."));
    }
    
    /**
     * 썸네일을 생성합니다.
     * 
     * @param imageId 원본 이미지 ID
     * @param width 썸네일 너비 (기본값: 300)
     * @param height 썸네일 높이 (기본값: 300)
     * @return 썸네일 이미지 정보
     */
    @PostMapping("/{imageId}/thumbnail")
    @Operation(summary = "썸네일 생성", description = "원본 이미지로부터 썸네일을 생성합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProductImageResponse>> generateThumbnail(
            @Parameter(description = "원본 이미지 ID", required = true) @PathVariable @Min(1) Long imageId,
            @Parameter(description = "썸네일 너비") @RequestParam(defaultValue = "300") @Min(50) Integer width,
            @Parameter(description = "썸네일 높이") @RequestParam(defaultValue = "300") @Min(50) Integer height) {
        
        ProductImageResponse thumbnail = productImageService.generateThumbnail(imageId, width, height);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("썸네일이 성공적으로 생성되었습니다.", thumbnail));
    }
    
    /**
     * 이미지 순서를 변경합니다.
     * 
     * @param imageId 이미지 ID
     * @param newOrder 새로운 순서
     * @return 순서 변경 확인 메시지
     */
    @PatchMapping("/{imageId}/order")
    @Operation(summary = "이미지 순서 변경", description = "특정 이미지의 순서를 변경합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "순서 변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> updateImageOrder(
            @Parameter(description = "이미지 ID", required = true) @PathVariable @Min(1) Long imageId,
            @Parameter(description = "새로운 순서", required = true) @RequestParam @Min(1) Integer newOrder) {
        
        productImageService.updateImageOrder(imageId, newOrder);
        
        return ResponseEntity.ok(ApiResponse.success("이미지 순서가 성공적으로 변경되었습니다."));
    }
    
    /**
     * 이미지들의 순서를 일괄 변경합니다.
     * 
     * @param productId 상품 ID
     * @param imageIds 새로운 순서대로 정렬된 이미지 ID 목록
     * @return 순서 변경 확인 메시지
     */
    @PatchMapping("/product/{productId}/reorder")
    @Operation(summary = "이미지 순서 일괄 변경", description = "상품 이미지들의 순서를 일괄로 변경합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "순서 변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> reorderImages(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId,
            @Parameter(description = "새로운 순서의 이미지 ID 목록", required = true) @RequestBody @NotEmpty List<Long> imageIds) {
        
        productImageService.reorderImages(productId, imageIds);
        
        return ResponseEntity.ok(ApiResponse.success("이미지 순서가 성공적으로 일괄 변경되었습니다."));
    }
    
    /**
     * 이미지 메타데이터를 조회합니다.
     * 
     * @param imageId 이미지 ID
     * @return 이미지 메타데이터
     */
    @GetMapping("/{imageId}/metadata")
    @Operation(summary = "이미지 메타데이터 조회", description = "이미지의 상세 메타데이터를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ImageMetadata>> getImageMetadata(
            @Parameter(description = "이미지 ID", required = true) @PathVariable @Min(1) Long imageId) {
        
        ImageMetadata metadata = productImageService.getImageMetadata(imageId);
        
        return ResponseEntity.ok(ApiResponse.success("이미지 메타데이터를 성공적으로 조회했습니다.", metadata));
    }
    
    /**
     * 이미지 타입을 변경합니다.
     * 
     * @param imageId 이미지 ID
     * @param newType 새로운 이미지 타입
     * @return 타입 변경 확인 메시지
     */
    @PatchMapping("/{imageId}/type")
    @Operation(summary = "이미지 타입 변경", description = "이미지의 타입을 변경합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "타입 변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 이미지 타입", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> updateImageType(
            @Parameter(description = "이미지 ID", required = true) @PathVariable @Min(1) Long imageId,
            @Parameter(description = "새로운 이미지 타입", required = true) @RequestParam @NotBlank String newType) {
        
        productImageService.updateImageType(imageId, newType);
        
        return ResponseEntity.ok(ApiResponse.success("이미지 타입이 성공적으로 변경되었습니다."));
    }
    
    /**
     * 임시 업로드된 이미지들을 정식으로 등록합니다.
     * 
     * @param productId 상품 ID
     * @param tempImageIds 임시 이미지 ID들
     * @return 정식 등록된 이미지 정보들
     */
    @PostMapping("/product/{productId}/confirm-temp")
    @Operation(summary = "임시 이미지 정식 등록", description = "임시로 업로드된 이미지들을 정식으로 등록합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 또는 임시 이미지를 찾을 수 없음", content = @Content(schema = @Schema(implementation = com.dodam.common.dto.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> confirmTempImages(
            @Parameter(description = "상품 ID", required = true) @PathVariable @Min(1) Long productId,
            @Parameter(description = "임시 이미지 ID 목록", required = true) @RequestBody @NotEmpty List<Long> tempImageIds) {
        
        List<ProductImageResponse> confirmedImages = productImageService.confirmTempImages(productId, tempImageIds);
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("%d개의 임시 이미지가 성공적으로 정식 등록되었습니다.", confirmedImages.size()), 
            confirmedImages));
    }
}