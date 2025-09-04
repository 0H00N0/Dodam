package com.dodam.product.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 상품 이미지 정보 응답 DTO
 * 클라이언트에게 전달하는 이미지 정보를 담습니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponse {

    /**
     * 이미지 ID
     */
    private Long id;

    /**
     * 상품 ID
     */
    private Long productId;

    /**
     * 파일 이름 (UUID 포함)
     */
    private String fileName;

    /**
     * 원본 파일 이름
     */
    private String originalFileName;

    /**
     * 이미지 타입 (MAIN, DETAIL, THUMBNAIL, REVIEW)
     */
    private String imageType;

    /**
     * 이미지 URL (접근 가능한 전체 URL)
     */
    private String imageUrl;

    /**
     * 썸네일 URL
     */
    private String thumbnailUrl;

    /**
     * 이미지 순서
     */
    private Integer orderIndex;

    /**
     * 이미지 설명
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
     * 활성 상태 여부
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 임시 업로드 여부
     */
    @Builder.Default
    private Boolean isTemporary = false;

    /**
     * 파일 크기 (bytes)
     */
    private Long fileSize;

    /**
     * MIME 타입
     */
    private String mimeType;

    /**
     * 이미지 너비 (픽셀)
     */
    private Integer width;

    /**
     * 이미지 높이 (픽셀)
     */
    private Integer height;

    /**
     * 썸네일 너비 (픽셀)
     */
    private Integer thumbnailWidth;

    /**
     * 썸네일 높이 (픽셀)
     */
    private Integer thumbnailHeight;

    /**
     * 체크섬 (파일 무결성 확인용)
     */
    private String checksum;

    /**
     * 태그들 (콤마 구분)
     */
    private String tags;

    /**
     * 업로드한 사용자 ID
     */
    private Long uploadedBy;

    /**
     * 업로드 일시
     */
    private LocalDateTime uploadedAt;

    /**
     * 수정 일시
     */
    private LocalDateTime modifiedAt;

    /**
     * 이미지 메타데이터 (상세 정보가 필요한 경우)
     */
    private ImageMetadata metadata;

    // 유틸리티 메서드

    /**
     * 파일 크기를 사람이 읽기 쉬운 형태로 반환
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "Unknown";
        }
        
        long bytes = fileSize;
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 이미지 해상도 문자열 반환 (예: "1920x1080")
     */
    public String getResolution() {
        if (width == null || height == null) {
            return "Unknown";
        }
        return width + "x" + height;
    }

    /**
     * 썸네일 해상도 문자열 반환 (예: "300x300")
     */
    public String getThumbnailResolution() {
        if (thumbnailWidth == null || thumbnailHeight == null) {
            return "Unknown";
        }
        return thumbnailWidth + "x" + thumbnailHeight;
    }

    /**
     * 파일 확장자 반환
     */
    public String getFileExtension() {
        if (fileName == null) {
            return null;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1).toLowerCase() : null;
    }

    /**
     * 이미지 종횡비 반환
     */
    public Double getAspectRatio() {
        if (width == null || height == null || height == 0) {
            return null;
        }
        return (double) width / height;
    }

    /**
     * 썸네일 존재 여부 확인
     */
    public Boolean hasThumbnail() {
        return thumbnailUrl != null && !thumbnailUrl.isEmpty();
    }

    /**
     * 이미지가 가로형인지 확인
     */
    public Boolean isLandscape() {
        Double aspectRatio = getAspectRatio();
        return aspectRatio != null && aspectRatio > 1.0;
    }

    /**
     * 이미지가 세로형인지 확인
     */
    public Boolean isPortrait() {
        Double aspectRatio = getAspectRatio();
        return aspectRatio != null && aspectRatio < 1.0;
    }

    /**
     * 이미지가 정사각형인지 확인
     */
    public Boolean isSquare() {
        Double aspectRatio = getAspectRatio();
        return aspectRatio != null && Math.abs(aspectRatio - 1.0) < 0.01;
    }

    /**
     * 대용량 파일인지 확인 (10MB 이상)
     */
    public Boolean isLargeFile() {
        return fileSize != null && fileSize > 10 * 1024 * 1024;
    }

    /**
     * 업로드 후 경과 시간 (분)
     */
    public Long getMinutesSinceUpload() {
        if (uploadedAt == null) {
            return null;
        }
        return java.time.Duration.between(uploadedAt, LocalDateTime.now()).toMinutes();
    }
}