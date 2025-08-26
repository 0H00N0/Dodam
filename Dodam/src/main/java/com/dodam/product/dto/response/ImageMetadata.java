package com.dodam.product.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 이미지 메타데이터 정보 DTO
 * 이미지 파일의 상세 정보를 담습니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageMetadata {

    /**
     * 파일 이름 (확장자 포함)
     */
    private String fileName;

    /**
     * 원본 파일 이름
     */
    private String originalFileName;

    /**
     * 파일 크기 (bytes)
     */
    private Long fileSize;

    /**
     * MIME 타입 (image/jpeg, image/png 등)
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
     * 이미지 해상도 (DPI)
     */
    private Integer resolution;

    /**
     * 색상 모드 (RGB, CMYK 등)
     */
    private String colorMode;

    /**
     * 비트 뎁스 (8, 16, 24 등)
     */
    private Integer bitDepth;

    /**
     * 압축 타입
     */
    private String compression;

    /**
     * EXIF 정보 (JSON 형태)
     */
    private String exifData;

    /**
     * GPS 위치 정보
     */
    private String gpsLocation;

    /**
     * 카메라 제조사
     */
    private String cameraMake;

    /**
     * 카메라 모델
     */
    private String cameraModel;

    /**
     * 촬영 일시
     */
    private LocalDateTime dateTaken;

    /**
     * ISO 감도
     */
    private Integer isoSpeed;

    /**
     * 조리개값 (f-stop)
     */
    private String aperture;

    /**
     * 셔터 속도
     */
    private String shutterSpeed;

    /**
     * 초점 거리 (mm)
     */
    private Integer focalLength;

    /**
     * 플래시 사용 여부
     */
    private Boolean flashUsed;

    /**
     * 이미지 방향 (1-8, EXIF 기준)
     */
    private Integer orientation;

    /**
     * 파일 체크섬 (MD5 또는 SHA256)
     */
    private String checksum;

    /**
     * 파일 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 파일 수정 일시
     */
    private LocalDateTime modifiedAt;

    /**
     * 썸네일 생성 여부
     */
    @Builder.Default
    private Boolean hasThumbnail = false;

    /**
     * 썸네일 파일 이름
     */
    private String thumbnailFileName;

    /**
     * 워터마크 적용 여부
     */
    @Builder.Default
    private Boolean hasWatermark = false;

    /**
     * 이미지 품질 점수 (0-100)
     */
    private Integer qualityScore;

    /**
     * 얼굴 감지 개수
     */
    private Integer faceCount;

    /**
     * 객체 감지 정보 (JSON 형태)
     */
    private String detectedObjects;

    /**
     * 색상 팔레트 (주요 색상들, JSON 형태)
     */
    private String colorPalette;

    /**
     * 이미지 태그들 (콤마 구분)
     */
    private String tags;

    /**
     * 추가 메타데이터 (JSON 형태)
     */
    private String additionalData;

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
     * 이미지 종횡비 반환
     */
    public Double getAspectRatio() {
        if (width == null || height == null || height == 0) {
            return null;
        }
        return (double) width / height;
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
}