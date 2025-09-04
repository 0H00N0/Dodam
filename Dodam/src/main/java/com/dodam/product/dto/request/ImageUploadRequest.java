package com.dodam.product.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 이미지 업로드 요청 DTO
 * 클라이언트에서 이미지 업로드 시 전달하는 정보를 담습니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadRequest {

    /**
     * 이미지 타입
     * MAIN: 메인 이미지
     * DETAIL: 상세 이미지  
     * THUMBNAIL: 썸네일
     * REVIEW: 리뷰 이미지
     */
    @NotBlank(message = "이미지 타입은 필수입니다")
    private String imageType;

    /**
     * 이미지 순서 (1부터 시작)
     */
    @NotNull(message = "이미지 순서는 필수입니다")
    @Min(value = 1, message = "이미지 순서는 1 이상이어야 합니다")
    @Max(value = 100, message = "이미지 순서는 100 이하여야 합니다")
    private Integer orderIndex;

    /**
     * 이미지 설명 (alt 텍스트)
     */
    private String description;

    /**
     * 이미지 제목
     */
    private String title;

    /**
     * 대표 이미지 여부
     */
    @Builder.Default
    private Boolean isMain = false;

    /**
     * 썸네일 자동 생성 여부
     */
    @Builder.Default
    private Boolean generateThumbnail = true;

    /**
     * 썸네일 너비 (생성 시)
     */
    @Builder.Default
    private Integer thumbnailWidth = 300;

    /**
     * 썸네일 높이 (생성 시)
     */
    @Builder.Default
    private Integer thumbnailHeight = 300;

    /**
     * 이미지 품질 (1-100, JPEG 압축용)
     */
    @Min(value = 1, message = "이미지 품질은 1 이상이어야 합니다")
    @Max(value = 100, message = "이미지 품질은 100 이하여야 합니다")
    @Builder.Default
    private Integer quality = 85;

    /**
     * 워터마크 적용 여부
     */
    @Builder.Default
    private Boolean applyWatermark = false;

    /**
     * 임시 업로드 여부
     */
    @Builder.Default
    private Boolean isTemporary = false;

    /**
     * 메타데이터 정보 (JSON 형태)
     */
    private String metadata;

    /**
     * 태그 목록 (콤마 구분)
     */
    private String tags;

    /**
     * 업로드 폴더 경로 (기본값: images/products)
     */
    @Builder.Default
    private String uploadPath = "images/products";

    /**
     * 파일명 접두사
     */
    private String filenamePrefix;

    /**
     * 파일명 접미사
     */
    private String filenameSuffix;

    // 유효성 검증 메서드
    public boolean isValidImageType() {
        return imageType != null && 
               (imageType.equals("MAIN") || 
                imageType.equals("DETAIL") || 
                imageType.equals("THUMBNAIL") || 
                imageType.equals("REVIEW"));
    }

    public boolean isValidThumbnailSize() {
        return thumbnailWidth != null && thumbnailHeight != null &&
               thumbnailWidth > 0 && thumbnailHeight > 0 &&
               thumbnailWidth <= 2000 && thumbnailHeight <= 2000;
    }
}